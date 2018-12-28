<!-- 左侧导航-->
<template>
   <Sider >
            <Menu class="leftbar-wrap" :class="menuitemClasses" :active-name="menuActiveName"  @on-select="menuSelect" theme="light">
                  <Submenu name="config">
                    <template slot="title">
                        <Icon type="ios-settings" />
                        <span>配置管理</span>
                    </template>
                    <MenuItem :name="menu.config.welcome">
                        <Icon type="logo-twitter" />
                        <span>欢迎页</span>
                    </MenuItem>
                    <MenuItem :name="menu.config.config" v-if="isSuperAdmin">
                        <Icon type="md-medical" />
                        <span>平台配置</span>
                    </MenuItem>
                </Submenu>
                <MenuItem name="/main/account/selfAccount">
                    <Icon type="ios-man" />
                    <span>个人信息</span>
                </MenuItem>
                 <Submenu name="teacherAccount" >
                    <template slot="title">
                        <Icon type="md-person" />
                        <span>账户管理</span>
                    </template>
                    <MenuItem name="/main/account/userAccount">
                        <Icon type="ios-body" />
                        <span>用户</span>
                    </MenuItem>
                </Submenu>
                <Submenu v-if="isSuperAdmin" name="managerAccount">
                    <template slot="title">
                        <Icon type="md-people" />
                        <span>管理员管理</span>
                    </template>
                    <MenuItem name="/main/account/managerAccount">
                        <Icon type="md-people" />
                        <span>管理员管理</span>
                    </MenuItem>
                </Submenu>
                <Submenu v-if="isSuperAdmin" name="rolePermission">
                    <template slot="title">
                        <Icon type="ios-people" />
                        <span>角色权限管理</span>
                    </template>
                    <MenuItem  :name="menu.rolePermission.role">
                        <Icon type="ios-man" />
                        <span>角色</span>
                    </MenuItem>
                    <MenuItem name="/main/permission">
                        <Icon type="md-sunny" />
                        <span>权限</span>
                    </MenuItem>
                </Submenu>
                <Submenu v-if="isSuperAdmin" name="system">
                    <template slot="title">
                        <Icon type="ios-people" />
                        <span>系统管理</span>
                    </template>
                    <MenuItem  name="/main/system/druid">
                        <Icon type="ios-man" />
                        <span>数据库监控</span>
                    </MenuItem>
                    <MenuItem  name="/main/system/swagger">
                        <Icon type="ios-man" />
                        <span>API接口管理(swagger)</span>
                    </MenuItem>
                </Submenu>
            </Menu>
        </Sider>
</template>
<script>
      export default {
    name: 'LeftBar',
    data () {
      return {
          isCollapsed: false,
          //菜单
          menu:{},
          //活动的菜单，即显示菜单
          menuActiveName:'/main/'

      }
    },
     computed: {
            menuitemClasses () {
                return [
                    'menu-item',
                    this.isCollapsed ? 'collapsed-menu' : ''
                ]
            }
        },
    methods: {
     menuSelect(name){
         this.$router.push(name);
         this.menuActiveName=name;
         //this.navList.splice(this.navList.indexOf(nav),1)
         //点击的菜单注册到滑道
         setTimeout(()=>{
             this.Hub.$emit('navroad',this.$router.currentRoute); //Hub触发事件
         },200)
     },
     //初始化菜单
     initMenu(){
         let pp=JSON.stringify(this.pathParams)
         this.menu={
             //配置
            config:{
                 welcome:"/main/welcome/"+pp,
                 config:"/main/config/"+pp,
                 activationCode:"/main/activationCode/"+pp,
                 contact:"/main/contact/"+pp
            },
            //角色权限
            rolePermission:{
                 role:"/main/role/"+pp
            },
         }
     }
    },
    watch:{

    },
    created(){
       //判断是否超级管理员
        this.isSuperAdmin=this.business.getIsSuperAdmin()
        this.pathParams={
          currentPage:1,//当前页
          //accountId:this.business.getAccount().accountId
        }
        this.initMenu();
    //this.isSuperAdmin= this.business.getIsSuperAdmin();
    //监听点击返回
    this.Hub.$on('routerChange', (msg) => { //Hub接收事件
        //this.msg = 'hehe';
       this.$nextTick(()=>{
            //this.menuActiveName=msg;
            this.menuSelect(msg)
        });
    });
    //监听切换toggleCollapse
    this.Hub.$on('isCollapsed', (msg) => { //Hub接收事件
       this.$nextTick(()=>{
             this.isCollapsed=msg
           //console.log(this.updateActiveName)
        });
    });
    }
  }
</script>
<style lang="less">
@import "LeftBar.less";

/**
**导航切换特效
*/
 .menu-item span{
        display: inline-block;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        vertical-align: bottom;
        transition: width .2s ease .2s;
    }
    .menu-item i:first-child{
        transform: translateX(0px);
        transition: font-size .2s ease, transform .2s ease;
        vertical-align: middle;
        font-size: 16px;
    }
    .collapsed-menu{
        width:76px !important;
        transition: width .2s ease;
        text-align:center !important;
    }
    .collapsed-menu li div{
        text-align:center !important;
    }
    .collapsed-menu span{
        font-size:0;
        width: 0px;
        transition: width .2s ease;
    }
    .collapsed-menu i:last-child:before{
        font-size:0;
        width: 0;
    }

    .collapsed-menu i:first-child{
        transition: font-size .2s ease .2s, transform .2s ease .2s;
        vertical-align: middle;
        font-size: 16px;

    }


    //修改子菜单位置
    .ivu-menu-vertical .ivu-menu-submenu .ivu-menu-item{
        padding-left:30px !important;
        height:50px !important;
    }
</style>
