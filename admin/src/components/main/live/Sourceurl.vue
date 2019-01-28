<!--来源url管理 -->
<template>
    <div class="body-wrap">
    <div class="body-btn-wrap">
       <Button type='primary'  @click='add'>增加来源url</Button> 
      <!-- <Button type='error'  @click='deleteBatch'>批量删除</Button> -->
      <Upload style="display:inline-block;"
          name="editorUpload"
          :show-upload-list="false"
          :action="upload.action"
          :on-progress="onProgess"
          :on-success="handleSuccess">
          <Button icon="ios-cloud-upload-outline">导入(格式：UTF-8)</Button>
          </Upload>
    </div>
		 <!--新增 -->
     <Modal v-model="addSourceurlModel"
           title="新增来源url管理"
           :closable="false"
           :mask-closable="false"
    >
      <Form ref="addSourceurl" :model="addSourceurl" :label-width="100" label-position="right"  :rules="addSourceurlRules">
        <FormItem prop="name" label="名称:">
          <Input type="text" v-model="addSourceurl.name" placeholder="名称">
          </Input>
        </FormItem>
        <FormItem prop="url" label="来源url:">
          <Input type="text" v-model="addSourceurl.url" placeholder="来源url">
          </Input>
        </FormItem>
      </Form>
      <div slot='footer'>
        <Button  @click='addCancel'>取消</Button>
        <Button type='primary' :loading='addLoading'>
          <span v-if="!addLoading" @click='addSure'>确定</span>
          <span v-else>Loading...</span>
        </Button>
      </div>
    </Modal>
    <!--新增end -->
		 <!--修改 -->
     <Modal v-model="updateSourceurlModel"
           title="修改来源url管理"
           :closable="false"
           :mask-closable="false"
    >
      <Form ref="updateSourceurl" :model="updateSourceurl" :label-width="100" label-position="right"  :rules="updateSourceurlRules">
        <FormItem prop="name" label="名称:">
          <Input type="text" v-model="updateSourceurl.name" placeholder="名称">
          </Input>
        </FormItem>
        <FormItem prop="url" label="来源url:">
          <Input type="text" v-model="updateSourceurl.url" placeholder="来源url">
          </Input>
        </FormItem>
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
      <Table border height="500" :columns='sourceurlColumns' :data='sourceurlList' ref='table' size="small"></Table>
        <div style='display: inline-block;float: right; margin-top:10px;'>
        <Page style='margin-right:10px;'  @on-page-size-change="onPageSizeChange" show-sizer :page-size-opts='params.pageSizeOpts' :current="params.currentPage" :total='params.total' :pageSize='params.pageSize' ref='page' :show-total='true'   @on-change='selectPage' show-elevator ></Page>
      </div>
    </div>
</template>
<script>
export default {
  name: 'Sourceurl',
  data () {
    return {
        //上传
      upload:{
        action:this.axios.defaults.imgURL+"/sourceurl/uploader",
      },
        params:{
            pageSizeOpts:[10,20,50,100,500,1000],//每页条数切换的配置
            startNum:1,//初始化个数
            currentPage:1,//当前页
            pageNum:1,//获取的第几个开始
            pageSize:10,//每页的个数
            total:0//总数
        },
			//增加参数
			addSourceurlModel:false,
			addLoading:false,
			addSourceurlRules: {
                name: [
                    {required: true, message: '名称为必填项', trigger: 'blur'}
                    ],
                url: [
                    {required: true, message: '来源url为必填项', trigger: 'blur'}
                    ]
                },
			addSourceurl:{
			},
			//修改参数
			updateSourceurlModel:false,
			updateLoading:false,
			updateSourceurlRules: {
                name: [
                    {required: true, message: '名称为必填项', trigger: 'blur'}
                    ],
                url: [
                    {required: true, message: '来源url为必填项', trigger: 'blur'}
                    ]
                },
			updateSourceurl:{
    		 "sourceurlId":1,
    		 "name":""
      },
      //删除参数
      deleteSourceurl:{},
	    sourceurlList: [],
	    sourceurlColumns: [
        {
          title: '序号',
          minWidth:100,
          align:'center',
          render: (h, params) => {
            return h('span', params.index
            +(this.params.currentPage-1)*this.params.pageSize+this.params.startNum);
          }
        },
        {
          title: '来源urlid',
          minWidth:100,
          key: 'sourceurlId',
          align:'center'
        },
        {
          title:'名称',
          minWidth:100,
        	key:'name',
          align:'center'
        },
        {
          title:'来源url',
          minWidth:100,
        	key:'url',
          align:'center'
        },
        {
          title:'创建时间',
          minWidth:100,
          key:'createDate',
          sortable: true,
          align:'center'
        },
        {
          title:'修改时间',
          minWidth:100,
          key:'updateDate',
          sortable: true,
          align:'center'
        },
				{
          title: '操作',
          minWidth:200,
          key: 'action',
          align:'center',
          render: (h, params) => {
            var varhh1=  h('Button', {
                props: {
                  type: 'primary',
                  size: 'small'
                },
                style: {
                  marginLeft: '10px'
                },
                on: {
                  click: () => {
                    this.update(params.row)
                  }
                }
              }, '编辑');
            var varhh2=  h('Button', {
                props: {
                  type: 'error',
                  size: 'small'
                },
                style: {
                  marginLeft: '10px'
                },
                on: {
                  click: () => {
                    this.delete(params.row)
                  }
                }
              }, '删除');
            	var s=h("div","");
			s=h("div",[
              varhh1,
              varhh2
            ]);
            return s;
          }
        }
      ],
    }
  },
  methods: {
      //上传时
    onProgess(event, file, fileList){
       this.$Spin.show();
    },
    //上传成功
        handleSuccess (res, file){
          if(res.code==200){
            this.getList()
          }
             this.$Spin.hide();
        },
    //分页点击
    selectPage (currentPage) {
      this.params.currentPage=currentPage;
      this.params.pageNum = (this.params.currentPage-1)*this.params.pageSize+this.params.startNum;
      //构造path
     let pp=JSON.stringify({
       currentPage:currentPage,
       accountId:JSON.parse(this.$route.params.pathParams).accountId
     })
     //console.log(this.$route.path.substr(0,this.$route.path.indexOf(this.$route.params.pathParams)))
      this.$router.push(this.$route.path.substr(0,this.$route.path.indexOf(this.$route.params.pathParams))+pp);
      this.getList()
    },
    //切换每页条数时的回调，返回切换后的每页条数
    onPageSizeChange(pageSize){
      this.getList(pageSize)
    },
  //获取列表
   getList (pageSize) {
     /**
     * 获取列表
     * $this  vue组件
     * p.countUrl 数量url
     * p.listUrl 列表url
     * p.data 返回列表
     */
    this.params.pageSize=pageSize||this.params.pageSize
     this.axiosbusiness.getList(this,{
       countUrl:'/sourceurl/count',
       listUrl:'/sourceurl/list',
       data:'sourceurlList'
     },this.params)
    },
  //增加
	 add (params) {
      this.addSourceurlModel = true
      this.addSourceurl.name = params.name
    },
		//增加取消
		 addCancel () {
      if (!this.addLoading) {
        this.addSourceurlModel = false
        this.$refs.addSourceurl.resetFields()
      }
    },
		//增加确定
    addSure () {
       /**
     * 增加
     * $this  vue组件
     * p.ref 验证
     * p.url 增加url
     * p.requestObject 请求参数对象
     * p.loading loading
     * p.showModel 界面模型显示隐藏
     */
    this.axiosbusiness.add(this,{
      ref:'addSourceurl',
      url:'/sourceurl/add',
      requestObject:'addSourceurl',
      loading:'addLoading',
      showModel:'addSourceurlModel'
    })
    },
	 update (params) {
      this.updateSourceurlModel = true
      //获取修改实体
      this.axiosbusiness.get(this,{
         url:'/sourceurl/load?sourceurlId='+params.sourceurlId,
         data:'updateSourceurl',
       })
    },
		//修改取消
		 updateCancel () {
      if (!this.updateLoading) {
        this.updateSourceurlModel = false
        this.$refs.updateSourceurl.resetFields()
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
    this.axiosbusiness.update(this,{
      ref:'updateSourceurl',
      url:'/sourceurl/update',
      requestObject:'updateSourceurl',
      loading:'updateLoading',
      showModel:'updateSourceurlModel'
    })
 
    },
    //删除
    delete(params){
    /**
     * 删除
     * $this  vue组件
     * p.url 修改url
     * p.requestObject 请求参数对象
     */
    this.deleteSourceurl={
      "sourceurlId":params.sourceurlId
    };
    this.axiosbusiness.delete(this,{
      url:'/sourceurl/delete',
      requestObject:'deleteSourceurl'
    })
    }
  },
     watch: {
    //当前页面参数修改动态启动
      $route (to,from){
        this.selectPage(JSON.parse(this.$route.params.pathParams).currentPage)
      }
    }, 
  created () {
    this.selectPage(JSON.parse(this.$route.params.pathParams).currentPage)
    //this.getList();
  },
  mounted () {

  }
}
</script>
