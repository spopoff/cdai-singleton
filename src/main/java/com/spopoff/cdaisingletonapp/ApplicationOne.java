/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spopoff.cdaisingletonapp;

import com.spopoff.application.IApplicationInstance;
import com.spopoff.application.IApplicationSession;
import com.spopoff.application.IUserSession;
import com.spopoff.ejb.DataSessionBeanRemote;
import com.spopoff.modelSrvlt.CustomException;
import com.spopoff.personMngt.Attribut;
import com.spopoff.personMngt.Identite;
import com.spopoff.statemachine.IActivityAlive;
import com.spopoff.statemachine.WorkflowController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author mrwc1264
 */
public class ApplicationOne implements IApplicationInstance{
    @EJB(beanName="DataSessionBeanRemote")
    DataSessionBeanRemote ejbData;
    private final WorkflowController ejbWkflw = new WorkflowController();
    private final Map<String, IApplicationSession> appSess = new HashMap<>();
    private final Map<String, Object> authentMap_ = new HashMap<>();
    
    private ApplicationOne(){
        if(ejbData==null){
            ejbData = findData();
        }
    }
    private static class ApplicationOneHolder {
        private final static ApplicationOne instance = new ApplicationOne();
    }
    public static ApplicationOne getInstance(){
        return ApplicationOneHolder.instance;
    }
    @Override
    public String getLoginMethod(String sessionId) {
        String ret = "GPLUS";
        IApplicationSession app = appSess.get(sessionId);
        if(app!=null){
            ret = app.getAuthMethod();
        }
        return ret;
    }

    @Override
    public void jSessionBegin(String sessionId) throws CustomException{
        try {
            ejbWkflw.beginWorkflow(sessionId);
        } catch (Exception e) {
            throw new CustomException(e.getMessage(), 21);
        }
    }

    @Override
    public boolean isJSessionBegin(String sessionId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public IUserSession getCurrentUserSession(String sessionId) {
        IApplicationSession app = appSess.get(sessionId);
        return app.getCurrentUserSession();
    }

    @Override
    public int logoutHandle(String sessionId) {
        int ret = 0;
        Iterator it2 = appSess.entrySet().iterator();
        while(it2.hasNext()){
            Map.Entry ent = (Map.Entry) it2.next();
            if(((String)ent.getKey()).equals(sessionId)){
                it2.remove();
            }
        }
        return ret;
    }

    @Override
    public void addApplicationSession(IApplicationSession app) {
        app.oneMoreTime();
        appSess.put(app.getClientSessionId(), app);
    }

    @Override
    public IApplicationSession getApplicationSession(String sessionId) {
        if(sessionId ==  null){
            return null;
        }
        IApplicationSession une = appSess.get(sessionId);
        if(une != null){
            une.oneMoreTime();
        }else{
            //une seconde chance si c'est une session serveur et un type anonyme
            for(Map.Entry<String, IApplicationSession> un : appSess.entrySet()){
                if(un.getValue().getServerSessionId().equals(sessionId) && 
                        un.getValue().getCurrentUserSession().getSessionType()==1){
                    une = un.getValue();
                    une.oneMoreTime();
                }
            }
        }
        return une;
    }
    @Override
    public Integer sessionTimeOut() {
        return 1000;
    }
    @Override
    public boolean isApplicationSession(String sessionId) {
        return appSess.containsKey(sessionId);
    }

    @Override
    public int getUserAccessCount(String sessionId) {
        return appSess.get(sessionId).getAccessUserCount();
    }

    @Override
    public String getApplicationSessions() {
        String ret = "";
        for(Map.Entry<String, IApplicationSession> une : appSess.entrySet()){
            ret += une.getKey() +"=" + une.getValue().toText()+"</br>";
        }
        return ret;
    }

    @Override
    public boolean existIdnt(int idIdnt) {
        if(ejbData==null){
            ejbData = findData();
        }
        return ejbData.existIdnt(idIdnt);
    }

    /**
     *
     * @param attrN
     * @param attrV
     * @return
     */
    @Override
    public boolean existIdnt(String attrN, String attrV) {
        if(ejbData==null){
            ejbData = findData();
        }
        boolean ret = false;
        List<Identite> toutes = null;
        try {
            toutes = ejbData.getAllIdentities();
        } catch (CustomException e) {
            return ret;
        }
        if(toutes==null){
            return false;
        }
        for(Identite une : toutes){
            if(une.getAttributMap().containsKey(attrN)){
                Attribut att = une.getAttributMap().get(attrN);
                if(((String)att.getValeur()).equals(attrV)){
                    ret = true;
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    public void addIdnt(Identite idnt) throws CustomException {
        if(ejbData==null){
            ejbData = findData();
        }
        ejbData.addIdnt(idnt);
    }
    /**
     * Met à jour une identité persistée et ses attributs
     * Attention les attributs ne possède pas la clé de persistance
     * @param idnt
     * @throws CustomException 
     */
    @Override
    public void updateIdnt(Identite idnt) throws CustomException {
        if(ejbData==null){
            ejbData = findData();
        }
        ejbData.updateIdnt(idnt);
    }

    @Override
    public Identite getIdnt(int idIdnt) throws CustomException{
        if(ejbData==null){
            ejbData = findData();
        }
        return ejbData.getIdnt(idIdnt);
    }
    @Override
    public Identite getIdnt(String attrN, String attrV) throws CustomException{
        if(ejbData==null){
            ejbData = findData();
        }
        Identite ret = null;
        List<Identite> toutes = null;
        try {
            toutes = ejbData.getAllIdentities();
        } catch (CustomException e) {
            throw e;
        }
        for(Identite une : toutes){
            if(une.getAttributMap().containsKey(attrN)){
                Attribut att = une.getAttributMap().get(attrN);
                if(((String)att.getValeur()).equals(attrV)){
                    ret = une;
                    break;
                }
            }
        }
        return ret;
    }

    @Override
    public List<Identite> getAllIdentities() throws CustomException {
        if(ejbData==null){
            ejbData = findData();
        }
        return ejbData.getAllIdentities();
    }

    @Override
    public void deleteIdnt(Identite idnt) throws CustomException {
        if(ejbData==null){
            ejbData = findData();
        }
        ejbData.deleteIdnt(idnt);
    }

    @Override
    public void clearSessionWorkflow(String jsess) throws CustomException {
        ejbWkflw.clearSessionWorkflow(jsess);
    }

    @Override
    public IActivityAlive getCurrentActivity(String jsess) {
        return ejbWkflw.getCurrentActivity(jsess);
    }

    @Override
    public String whatNext(String jsess) {
        return ejbWkflw.whatNext(jsess, getLoginMethod(jsess));
    }
    /**
     * La session JSESSION est-elle authentifiÃ©e ?
     *
     * @return boolean - session existe ou pas
     */
    @Override
    public boolean isAuthenticated(String jsess) {
        return (authentMap_.containsKey(jsess)||appSess.containsKey(jsess));
    }

    /**
     * Enregistre le token / preuve d'authentification
     *
     * @param jsess
     * @param token
     */
    @Override
    public void setAuthenticationToken(String jsess, Object token) {
        if (!authentMap_.containsKey(jsess)) {
            authentMap_.put(jsess, token);
        }
    }

    /**
     * retourne l'objet qui sert de preuve d'authentification ou n'importe quoi
     * utile Ã  l'authentification
     *
     * @param jsess
     * @return
     */
    @Override
    public Object getAuthenticationToken(String jsess) {
        Object ret = null;
        if (authentMap_.containsKey(jsess)) {
            ret = authentMap_.get(jsess);
        }
        return ret;
    }

    @Override
    public void setTerminationStatus(boolean valid, String jsess, String action) throws Exception {
        ejbWkflw.setTerminationStatus(valid, jsess, action);
    }
    /**
     * méthode traditionnelle de chargement des EJB
     * @return 
     */
    private DataSessionBeanRemote findData(){
        DataSessionBeanRemote appData = null;
        InitialContext ic = null;
        try {
            ic = new InitialContext();
        } catch (NamingException ex) {
            System.err.println("Erreur contexte jndi "+ex.toString());
            return null;
        }
        System.out.println("initialise IApplicationInstance");
        try {
            appData = (DataSessionBeanRemote) ic.lookup("java:global/cdaiProjetEjbData-1.1.0-SNAPSHOT/DataSessionBean!com.spopoff.ejb.DataSessionBeanRemote");
        } catch (NamingException err) {
            System.err.println("Erreur DataSessionBeanRemote contexte jndi "+ err);
            //return;
        } catch (Exception ex) {
            System.err.println("Erreur DataSessionBeanRemote "+ ex);
            //return;
        }
        return appData;
    }
    
}
