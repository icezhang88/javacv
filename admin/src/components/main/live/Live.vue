<!--直播管理 -->
<template>
    <div class="body-wrap">
    <div class="body-btn-wrap">
       <Button type='primary'  @click='add'>增加直播</Button>
    </div>
		 <!--新增 -->
     <Modal v-model="addLiveModel"
           title="新增直播管理"
           :closable="false"
           :mask-closable="false"
    >
      <Form ref="addLive" :model="addLive" :label-width="100" label-position="right"  :rules="addLiveRules">
        <FormItem prop="name" label="名称:">
          <Input type="text" v-model="addLive.name" placeholder="名称">
          </Input>
        </FormItem>
        <FormItem prop="sourceUrl" label="来源url:">
          <Input type="text" v-model="addLive.sourceUrl" placeholder="来源url">
          </Input>
        </FormItem>
        <FormItem prop="targetUrl" label="目的url:">
          <Input type="text" v-model="addLive.targetUrl" placeholder="目的url">
          </Input>
        </FormItem>
        <FormItem prop="width" label="宽:">
          <Input type="text" v-model="addLive.width" placeholder="宽">
          </Input>
        </FormItem>
        <FormItem prop="height" label="高:">
          <Input type="text" v-model="addLive.height" placeholder="高">
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
     <Modal v-model="updateLiveModel"
           title="修改直播管理"
           :closable="false"
           :mask-closable="false"
    >
      <Form ref="updateLive" :model="updateLive" :label-width="100" label-position="right"  :rules="updateLiveRules">
        <FormItem prop="name" label="名称:">
          <Input type="text" v-model="updateLive.name" placeholder="名称">
          </Input>
        </FormItem>
        <FormItem prop="sourceUrl" label="来源url:">
          <Input type="text" v-model="updateLive.sourceUrl" placeholder="来源url">
          </Input>
        </FormItem>
        <FormItem prop="targetUrl" label="目的url:">
          <Input type="text" v-model="updateLive.targetUrl" placeholder="目的url">
          </Input>
        </FormItem>
        <FormItem prop="width" label="宽:">
          <Input type="text" v-model="updateLive.width" placeholder="宽">
          </Input>
        </FormItem>
        <FormItem prop="height" label="高:">
          <Input type="text" v-model="updateLive.height" placeholder="高">
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
      <Table border height="500" :columns='liveColumns' :data='liveList' ref='table' size="small"></Table>
        <div style='display: inline-block;float: right; margin-top:10px;'>
        <Page style='margin-right:10px;'  @on-page-size-change="onPageSizeChange" show-sizer :page-size-opts='params.pageSizeOpts' :current="params.currentPage" :total='params.total' :pageSize='params.pageSize' ref='page' :show-total='true'   @on-change='selectPage' show-elevator ></Page>
      </div>
    </div>
</template>
<script>
export default {
  name: 'Live',
  data () {
    return {
        params:{
            pageSizeOpts:[10,20,50,100,500,1000],//每页条数切换的配置
            startNum:1,//初始化个数
            currentPage:1,//当前页
            pageNum:1,//获取的第几个开始
            pageSize:10,//每页的个数
            total:0//总数
        },
			//增加参数
			addLiveModel:false,
			addLoading:false,
			addLiveRules: {
                name: [
                    {required: true, message: '名称为必填项', trigger: 'blur'}
                    ],
                sourceUrl: [
                    {required: true, message: '来源url为必填项', trigger: 'blur'}
                    ],
                targetUrl: [
                    {required: true, message: '目的url为必填项', trigger: 'blur'}
                    ],
                },
			addLive:{
			},
			//修改参数
			updateLiveModel:false,
			updateLoading:false,
			updateLiveRules: {
                name: [
                    {required: true, message: '名称为必填项', trigger: 'blur'}
                    ],
                sourceUrl: [
                    {required: true, message: '来源url为必填项', trigger: 'blur'}
                    ],
                targetUrl: [
                    {required: true, message: '目的url为必填项', trigger: 'blur'}
                    ],
                },
			updateLive:{
    		 "liveId":1,
      },
      //删除参数
      deleteLive:{},
	    liveList: [],
	    liveColumns: [
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
          title: '直播id',
          minWidth:100,
          key: 'liveId',
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
        	key:'sourceUrl',
          align:'center'
        },
        {
          title:'目的url',
          minWidth:100,
        	key:'targetUrl',
          align:'center'
        },
        {
          title:'宽',
          minWidth:100,
        	key:'width',
          align:'center'
        },
        {
          title:'高',
          minWidth:100,
        	key:'height',
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
       countUrl:'/live/count',
       listUrl:'/live/list',
       data:'liveList'
     },this.params)
    },
  //增加
	 add (params) {
      this.addLiveModel = true
      this.addLive.name = params.name
    },
		//增加取消
		 addCancel () {
      if (!this.addLoading) {
        this.addLiveModel = false
        this.$refs.addLive.resetFields()
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
      ref:'addLive',
      url:'/live/add',
      requestObject:'addLive',
      loading:'addLoading',
      showModel:'addLiveModel'
    })
    },
	 update (params) {
      this.updateLiveModel = true
      //获取修改实体
      this.axiosbusiness.get(this,{
         url:'/live/load?liveId='+params.liveId,
         data:'updateLive',
       })
    },
		//修改取消
		 updateCancel () {
      if (!this.updateLoading) {
        this.updateLiveModel = false
        this.$refs.updateLive.resetFields()
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
      ref:'updateLive',
      url:'/live/update',
      requestObject:'updateLive',
      loading:'updateLoading',
      showModel:'updateLiveModel'
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
    this.deleteLive={
      "liveId":params.liveId
    };
    this.axiosbusiness.delete(this,{
      url:'/live/delete',
      requestObject:'deleteLive'
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
  }
}
</script>
