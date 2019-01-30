import Vue from 'vue'
import Router from 'vue-router'


Vue.use(Router)

const router= new Router({
  // export default new Router({
  mode: 'history',
  routes: [
    {
      path: '/',
      name: '首页',
      redirect: "/login"
    },
 /*   {
      path: '/index',
      name: '首页',
      component: resolve=>require(['@/components/Index'],resolve)
    },*/
    {
      path: '/login',
      name: '登录',
      component: resolve=>require(['@/components/Login'],resolve)
    },
    
    {
      path: '/main',
      name: '主页',
      component: resolve=>require(['@/components/main/Main'],resolve),
      children:[
        {
          path: 'welcome/:pathParams',
          name: '欢迎页',
          component: resolve=>require(['@/components/main/config/Welcome'],resolve)
        },
        {
          path: 'config/:pathParams',
          name: '平台配置',
          component: resolve=>require(['@/components/main/config/Config'],resolve)
        },
        {
          path: 'scheduleJob/:pathParams',
          name: '计划任务管理',
          component: resolve=>require(['@/components/main/scheduleJob/ScheduleJob'],resolve)
        },
        {
          path: 'account/selfAccount',
          name: '个人信息',
          component: resolve=>require(['@/components/main/account/SelfAccount'],resolve),
        },
        {
          path: 'sourceurl/:pathParams',
          name: '来源url',
          component: resolve=>require(['@/components/main/live/Sourceurl'],resolve)
        },
        {
          path: 'live/:pathParams',
          name: '直播(手动)',
          component: resolve=>require(['@/components/main/live/Live'],resolve)
        },
        {
          path: 'live2/:pathParams',
          name: '直播(自动)',
          component: resolve=>require(['@/components/main/live/Live2'],resolve)
        },
        {
          path: 'account/managerAccount',
          name: '管理员',
          component: resolve=>require(['@/components/main/account/ManagerAccount'],resolve),
        },
        {
          path: 'account/userAccount',
          name: '用户',
          component: resolve=>require(['@/components/main/account/UserAccount'],resolve),
        },
        {
          path: 'integral/:pathParams',
          name: '积分',
          component: resolve=>require(['@/components/main/account/Integral'],resolve),
        },
         {
          path: 'role/:pathParams',
          name: '角色',
          component: resolve=>require(['@/components/main/rolePermission/Role'],resolve)
        },
        {
          path: 'permission',
          name: '权限',
          component: resolve=>require(['@/components/main/rolePermission/Permission'],resolve)
        },
        {
          path: 'rolePermission/:roleId',
          name: '角色权限',
          component: resolve=>require(['@/components/main/rolePermission/RolePermission'],resolve)
        },
       
        {
          path: 'system/druid',
          name: '数据库监控',
          component: resolve=>require(['@/components/main/system/Druid'],resolve)
        },
        {
          path: 'system/swagger',
          name: 'API接口管理（swagger）',
          component: resolve=>require(['@/components/main/system/Swagger'],resolve)
        }
      ]
    }
  ]
})
/* router.beforeEach((to, from, next) => {
  if(to.fullPath.indexOf("role")>0
    ||to.fullPath.indexOf("permission")>0
    ||to.fullPath.indexOf("rolePermission")>0){
      //判断是否超级管理员，是就显示账户管理
      if(sessionStorage.getItem("account")){
        let account=JSON.parse(sessionStorage.getItem("account"));
        if(account.role.name=="超级管理员"){
        next()
      }else{
        next(false)
      }
    }else{
      next()
    }

  }else{
    next()
  }

}) */
export default router
