<!--个人信息管理 -->
<template>
    <div class="body-wrap">
    <div class="body-btn-wrap">
      <Button type='primary' @click='update(updateAccount)'>修改个人信息</Button>
      <Button type='error' @click='updatePassword(updatePasswordAccount)'>修改密码</Button>
    </div>
		 <!--修改 -->
     <Modal v-model="updateAccountModel"
           title="修改个人信息管理"
           :closable="false"
           :mask-closable="false"
    >
     <Form ref="updateAccount" :model="updateAccount" :label-width="100" label-position="right"  :rules="updateAccountRules">
          <FormItem prop="phone" label="手机:">
          <Input type="text" v-model="updateAccount.phone" placeholder="手机">
          </Input>
        </FormItem>
        <!-- <FormItem prop="password" label="密码:">
          <Input type="password" v-model="updateAccount.password" placeholder="密码">
          </Input>
        </FormItem> -->
       <FormItem prop="realname" label="姓名:">
          <Input type="text" v-model="updateAccount.realname" placeholder="姓名">
          </Input>
        </FormItem>
        <FormItem prop="sex" label="性别:">
          <Select v-model="updateAccount.sex" transfer size="large" style="width:100px">
              <Option v-for="item in sexList" :value="item.id" :key="item.id">{{ item.value }}</Option>
          </Select>
        </FormItem>


        <!-- <FormItem prop="icon" label="图像(上传或者填写):">
            <my-upload :defaultUpload="addAccount.icon" @uploadList="getAddIcon"></my-upload>
            <div>
              <Input type="text" v-model="addAccount.icon" placeholder="图像">
            </Input>
              <img :src="addAccount.icon"  style='width:30px;'alt="">
            </div>
        </FormItem> -->
       <!--  <FormItem prop="status" label="状态:">
          <Select v-model="updateAccount.status" transfer size="large" style="width:100px">
              <Option v-for="item in statusList" :value="item.id" :key="item.id">{{ item.value }}</Option>
          </Select>
        </FormItem> -->
        <!-- <FormItem prop="roleId" label="角色:">
          <Select v-model="updateAccount.roleId"  transfer size="large" style="width:100px">
              <Option v-for="item in roleList" :value="item.roleId" :key="item.roleId">{{ item.name }}</Option>
          </Select>
        </FormItem> -->
      </Form>
      <div slot='footer'>
        <Button  @click='updateCancel'>取消</Button>
        <Button type='primary' :loading='updateLoading'>
          <span v-if="!updateLoading" @click='updateSure'>确定</span>
          <span v-else>Loading...</span>
        </Button>
      </div>
    </Modal>
    <!--修改end -->
     <!--修改 密码-->
     <Modal v-model="updatePasswordAccountModel"
           title="修改密码"
           :closable="false"
           :mask-closable="false"
    >
    <Form ref="updatePasswordAccount" :model="updatePasswordAccount" :label-width="100" label-position="right"  :rules="updatePasswordAccountRules">
        <FormItem prop="password" label="旧密码:">
          <Input type="password" v-model="updatePasswordAccount.oldPassword" placeholder="旧密码">
          </Input>
        </FormItem>
        <FormItem prop="password" label="新密码:">
          <Input type="password" v-model="updatePasswordAccount.password" placeholder="新密码">
          </Input>
        </FormItem>
        <FormItem prop="password2" label="确认密码:">
          <Input type="password" v-model="updatePasswordAccount.password2" placeholder="确认密码">
          </Input>
        </FormItem>
      </Form>
      <div slot='footer'>
        <Button  @click='updatePasswordCancel'>取消</Button>
        <Button type='primary' :loading='updatePasswordLoading'>
          <span v-if="!updatePasswordLoading" @click='updatePasswordSure'>确定</span>
          <span v-else>Loading...</span>
        </Button>
      </div>
    </Modal>
     <Card v-if="!updateAccount.accountId" style="text-align:center">暂无</Card>
      <Card v-if="updateAccount.accountId" :bordered="true">
            <p slot="title">个人信息</p>
            <p>
              <span>姓名：</span>
              <span v-text="updateAccount.realname"></span>
            </p>
            <p>
              <span>性别：</span>
              <span>{{updateAccount.sex==0?'未知':updateAccount.sex==1?'男性':'女性'}}</span>
            </p>
            <p>
              <span>手机：</span>
              <span v-text="updateAccount.phone"></span>
            </p>
            <!-- <p>
              <span>图像：</span>
              <img v-text="updateAccount.icon" style="width:100px;height:100px;"/>
            </p> -->
            <p>
              <span>创建时间：</span>
              <span v-text="updateAccount.createDate"></span>
            </p>
            <p>
              <span>最后登录时间：</span>
              <span v-text="updateAccount.loginDate"></span>
            </p>
      </Card>
    </div>
</template>
<script>
export default {
  name: 'SelfAccount',
  data () {
    return {
      isSuperAdmin:this.business.getIsSuperAdmin(),
        params:{
            startNum:1,//初始化个数
            currentPage:1,//当前页
            pageNum:1,//获取的第几个开始
            pageSize:10,//每页的个数
            total:0//总数
        },

        //性别
        sexList:[
        {id:0,value:'未知'},
        {id:1,value:'男性'},
        {id:2,value:'女性'}
        ],
      //状态
      statusList:[
        {id:0,value:'正常'},
        {id:1,value:'封禁'},
        {id:2,value:'异常'}
        ],
			//修改参数
			updateAccountModel:false,
			updateLoading:false,
			updateAccountRules: {
                phone: [
                    {required: true, message: '手机', trigger: 'blur'}
                    ]
                },
			updateAccount:{ },
			//修改密码
			updatePasswordAccountModel:false,
			updatePasswordLoading:false,
			updatePasswordAccountRules: {
                oldPassowrd: [
                    {required: true, message: '旧密码必填', trigger: 'blur'}
                    ],
                passowrd: [
                    {required: true, message: '新密码必填', trigger: 'blur'}
                    ],
                passowrd2: [
                    {required: true, message: '确认密码必填', trigger: 'blur'}
                    ],
                },
			updatePasswordAccount:{ },
    }
  },
  methods: {
	 update (params) {
      this.updateAccountModel = true
      console.log(params)
     //获取修改实体
       this.axiosbusiness.get(this,{
         url:'/account/load?accountId='+this.business.getAccount().accountId,
         data:'updateAccount',
         success:()=>{}
         })
    },
		//修改取消
		 updateCancel () {
      if (!this.updateLoading) {
        this.updateAccountModel = false
       // this.$refs.updateAccount.resetFields()
      }
    },
		//修改确定
    updateSure () {
      /**
     * 修改
     * $this  vue组件
     * p.ref 验证
     * p.url 修改url
     * p.requestObject 请求参数对象
     * p.loading loading
     * p.showModel 界面模型显示隐藏
     */
    delete this.updateAccount.role
    this.updateAccount.birthDate=this.utils.getTime(this.updateAccount.birthDate)
    this.axiosbusiness.update(this,{
      ref:'updateAccount',
      url:'/account/update',
      requestObject:'updateAccount',
      loading:'updateLoading',
      showModel:'updateAccountModel',
       success:()=>{}
    })

    },


        ////修改密码

        updatePassword (params) {
      this.updatePasswordAccountModel = true
     //获取修改实体
     if(params.accountId){
       this.axiosbusiness.get(this,{
         url:'/account/load?accountId='+params.accountId,
         data:'updatePasswordAccount',
          success:()=>{}
         })
     }
    },
		//修改取消
		 updatePasswordCancel () {
      if (!this.updatePasswordLoading) {
        this.updatePasswordAccountModel = false
       // this.$refs.updatePasswordAccount.resetFields()
      }
    },
		//修改确定
    updatePasswordSure () {
      /**
     * 修改
     * $this  vue组件
     * p.ref 验证
     * p.url 修改url
     * p.requestObject 请求参数对象
     * p.loading loading
     * p.showModel 界面模型显示隐藏
     */
    this.updatePasswordAccount.accountId=this.business.getAccount().accountId
    if(this.updatePasswordAccount.password!=this.updatePasswordAccount.password2){
        this.$Message.error('两次密码不一致')
        return;
    }
    this.axiosbusiness.update(this,{
      ref:'updatePasswordAccount',
      url:'/account/updatePassword',
      requestObject:'updatePasswordAccount',
      loading:'updatePasswordLoading',
      showModel:'updatePasswordAccountModel',
       success:()=>{}
    })

    },
  },
  created () {
      this.updateAccount=this.business.getAccount();
  },
  mounted () {

  }
}
</script>
