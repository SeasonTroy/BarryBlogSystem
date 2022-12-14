package com.bootdang.shiro;
import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import com.bootdang.shiro.MyModularRealmAuthenticator;

import javax.servlet.Filter;
import java.util.*;


@Configuration
public class shiroConfig {

    @Bean(name="shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(){
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
         shiroFilterFactoryBean.setLoginUrl("/login");
         shiroFilterFactoryBean.setUnauthorizedUrl("/autherror");
        Map<String, Filter> objectObjectHashMap = new HashMap<>();
         //objectObjectHashMap.put("kickout",kickoutSessionControlFilter(sessionListenersUser()));
          objectObjectHashMap.put("rembuser",new UserSetting());
         shiroFilterFactoryBean.setFilters(objectObjectHashMap);//???????????????????????????

         shiroFilterFactoryBean.setSecurityManager( securityManager());
          Map<String, String> objectObjectLinkedHashMap = new LinkedHashMap<>();
          objectObjectLinkedHashMap.put("/homestatic/**","anon");
        objectObjectLinkedHashMap.put("/","anon");
        objectObjectLinkedHashMap.put("/admin/login","anon");
        objectObjectLinkedHashMap.put("/static/**","anon");
        objectObjectLinkedHashMap.put("/login","anon");
        objectObjectLinkedHashMap.put("/admin/yzm","anon");
        objectObjectLinkedHashMap.put("/admin/code","anon");
        objectObjectLinkedHashMap.put("/upload/**","anon");
        objectObjectLinkedHashMap.put("/swagger-ui.html","anon");
          objectObjectLinkedHashMap.put("/autherror","anon");
          objectObjectLinkedHashMap.put("/admin/logout","anon");
        objectObjectLinkedHashMap.put("/error/404.html","anon");
        objectObjectLinkedHashMap.put("/unauthorized/**","anon");
        objectObjectLinkedHashMap.put("/admin/sendEmail/*","anon");
          objectObjectLinkedHashMap.put("/admin/**","user");
        objectObjectLinkedHashMap.put("/**","anon,rembuser");


        shiroFilterFactoryBean.setFilterChainDefinitionMap(objectObjectLinkedHashMap);
     return shiroFilterFactoryBean;
    }
    //shiro???????????????
    @Bean
    public SecurityManager securityManager(){
        DefaultSecurityManager defaultSecurityManager = new DefaultWebSecurityManager();
       // defaultSecurityManager.setSessionManager();
        defaultSecurityManager.setAuthenticator(myModularRealmAuthenticator());
        List<Realm> objects = new ArrayList<>();
        objects.add(myrealm());
        objects.add(myadminrealm());

        defaultSecurityManager.setRealms(objects);
        defaultSecurityManager.setRememberMeManager(rememberMeManager());
        defaultSecurityManager.setSessionManager(sessionManager());
        defaultSecurityManager.setCacheManager(shiroehcacheManager());

        return defaultSecurityManager;
    }
    //???????????????realm??????
    @Bean
    public MyModularRealmAuthenticator myModularRealmAuthenticator(){
        MyModularRealmAuthenticator myModularRealmAuthenticator = new MyModularRealmAuthenticator();
        myModularRealmAuthenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
        return myModularRealmAuthenticator;
    }
    //?????????realm
    @Bean
    public Realm myrealm(){
        myRealm myRealm = new myRealm();
        myRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        myRealm.setAuthenticationCachingEnabled(true);//????????????
        myRealm.setAuthorizationCachingEnabled(true);//????????????
        return myRealm;
    }
    @Bean
    public Realm myadminrealm(){
        myAdminRealm myRealm = new myAdminRealm();
        myRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        myRealm.setAuthenticationCachingEnabled(true);
        myRealm.setAuthorizationCachingEnabled(false);
        return myRealm;
    }
  //????????????
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher(){
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("MD5");
        hashedCredentialsMatcher.setHashIterations(20);
        return hashedCredentialsMatcher;
    }


    //???????????????
    @Bean
    public RememberMeManager rememberMeManager(){
        CookieRememberMeManager rememberMeManager = new CookieRememberMeManager();
        rememberMeManager.setCookie(rememberMeCookie());
        rememberMeManager.setCipherKey(Base64.getDecoder().decode("2AvVhdsgUs0FSA3SDFAdag=="));
        return rememberMeManager;
    }
    @Bean
    public SimpleCookie rememberMeCookie(){
        SimpleCookie simpleCookie = new SimpleCookie();
        simpleCookie.setName("jlbk.shiro.rememberme");
        simpleCookie.setHttpOnly(true);
        simpleCookie.setPath("/");
        simpleCookie.setMaxAge(864000);
        return simpleCookie;
    }

    /*public CacheManager ehcacheManager(){
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManagerConfigFile("classpath:config/ehcache.xml");
        return ehCacheManager;
    }*/
    @Bean
    public SessionListener mysessionListener(){
        return new SessionListenersUser();
    }

    @Bean//????????????session?????????
    public SessionManager sessionManager(){
        DefaultWebSessionManager defaultWebSessionManager = new DefaultWebSessionManager();
        List<SessionListener> sessionListeners = new ArrayList<>();
        sessionListeners.add(mysessionListener());

        defaultWebSessionManager.setSessionListeners(sessionListeners);//session?????????
        defaultWebSessionManager.setSessionIdCookie(sessionCookie());//sessionid??????
        defaultWebSessionManager.setSessionIdCookieEnabled(true);
        //defaultWebSessionManager.setSession
        defaultWebSessionManager.setSessionDAO(sessionDAO());
        defaultWebSessionManager.setGlobalSessionTimeout(3600000);//60??????
        defaultWebSessionManager.setDeleteInvalidSessions(true);//????????????session
        defaultWebSessionManager.setSessionValidationSchedulerEnabled(true);//????????????session????????????
        defaultWebSessionManager.setSessionValidationInterval(100000);//session?????????????????????????????????????????????10??????
        defaultWebSessionManager.setSessionIdUrlRewritingEnabled(false);//??????url ????????? JSESSIONID

        return defaultWebSessionManager;
    }

    /**
     * ??????Shiro?????????????????????
     * @return
     */
    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * ?????????thymeleaf????????????shiro?????????????????????????????????
     * ?????????thymeleaf??????Caused by: java.lang.ClassNotFoundException: org.thymeleaf.dialect.AbstractProcessorDialect
     * @return
     */
    @Bean
    public ShiroDialect shiroDialect() {
        return new ShiroDialect();
    }


    @Bean
    public SessionDAO sessionDAO(){
        EnterpriseCacheSessionDAO enterpriseCacheSessionDAO = new EnterpriseCacheSessionDAO();
        enterpriseCacheSessionDAO.setCacheManager(shiroehcacheManager());
        enterpriseCacheSessionDAO.setActiveSessionsCacheName("jlbk.shiro.systemcache");
        enterpriseCacheSessionDAO.setSessionIdGenerator(sessionIdGenerator());
        return enterpriseCacheSessionDAO;
    }


    @Bean//session ???key????????????
    public SessionIdGenerator sessionIdGenerator(){
        return new JavaUuidSessionIdGenerator();
    }

    @Bean//shiro???ehcache?????????
    public EhCacheManager shiroehcacheManager(){
        EhCacheManager ss= new EhCacheManager();
       // ehCacheManager.setCacheManager();
        //ss.setCacheManager(cacheManager());
        ss.setCacheManagerConfigFile("classpath:config/ehcache.xml");


        return ss;
    }
    @Primary
    @Bean
    public CacheManager shirocacheManager(){
        return CacheManager.create();
    }

    @Bean//session???cookie??????
    public SimpleCookie sessionCookie(){
        SimpleCookie simpleCookie = new SimpleCookie("jlbk.shiro.sessionid");
        simpleCookie.setMaxAge(-1);
        simpleCookie.setHttpOnly(true);
        simpleCookie.setPath("/");
        return simpleCookie;
    }

    /*
     * shiro???????????????
     * */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SessionListenersUser sessionListenersUser){
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }

    @Bean//?????????????????????
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        return defaultAdvisorAutoProxyCreator;
    }
    /*
     * ?????????????????????????????????
     * */
   /* @Bean
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver(){
        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();
        Properties properties = new Properties();
       // properties.setProperty("UnauthorizedException","403");
        properties.setProperty("org.apache.shiro.authz.UnauthorizedException","/unauthorized/unauthorized");
        properties.setProperty("org.apache.shiro.authz.UnauthenticatedException","/unauthorized/unauthorized");
        simpleMappingExceptionResolver.setExceptionMappings(properties);
        return simpleMappingExceptionResolver;
    }*/
    /**
     * ??????????????????
     * @return
     */
    @Bean
    public KickoutSessionControlFilter kickoutSessionControlFilter(SessionListenersUser sessionListenersUser){
        KickoutSessionControlFilter kickoutSessionControlFilter = new KickoutSessionControlFilter();
        //??????????????????ID???????????????????????????????????????
        kickoutSessionControlFilter.setSessionManager(sessionManager());
        //??????cacheManager???????????????cache?????????????????????????????????????????????????????????????????????????????????
        kickoutSessionControlFilter.setCacheManager(shiroehcacheManager());
        //???????????????????????????????????????false?????????????????????????????????????????????????????????
        kickoutSessionControlFilter.setKickoutAfter(false);
        //??????????????????????????????????????????1?????????2???????????????????????????????????????????????????????????????
        kickoutSessionControlFilter.setMaxSession(1);
        //????????????????????????????????????
        kickoutSessionControlFilter.setKickoutUrl("/login?kickout=1");
        return kickoutSessionControlFilter;
    }




}
