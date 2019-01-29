<!--直播管理 -->
<template>
    <div class="body-wrap">
    <div class="body-btn-wrap">
        <Button type='primary'  @click='add'>增加直播</Button>
        <Button type='success' style="margin:0 20px;" @click='startAllLive'>全部开启</Button>
        <Button type='info' style="margin:0 20px;" @click='startBatchLive'>批量开启</Button>
        <Button type='error' style="margin:0 20px;" @click='stopAllLive'>全部停止</Button>
        <Button type='warning' style="margin:0 20px;" @click='stopBatchLive'>批量停止</Button>
    </div>
		 <!--新增 -->
     <Modal v-model="addLiveModel"
           title="新增直播管理"
           :closable="false"
           :mask-closable="false"
           width="800px"
    >
      <Form ref="addLive" :model="addLive" :label-width="100" label-position="right"  :rules="addLiveRules">
        <FormItem prop="sourceurl" label="选择来源:">
          <Select v-model="addLive.sourceurlId" @on-change="selectAddSourceurl" transfer size="large" >
              <Option v-for="item in sourceurlList" :value="item.sourceurlId" :key="item.sourceurlId">{{ item.name+"   "+item.url }}</Option>
          </Select>
        </FormItem>
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
        <FormItem prop="playUrl" label="播放url:">
          <Input type="text" v-model="addLive.playUrl" placeholder="播放url">
          </Input>
        </FormItem>
        <FormItem prop="model" label="模式:">
        <RadioGroup v-model="addLive.model"  type="button" >
            <Radio  style="margin:5px;border-left:1px solid #dddee1" :label="item.id" v-for="item in modelList" :value="item.id" :key="item.id" >
                {{item.value}}
            </Radio>
        </RadioGroup>
        </FormItem>
        <FormItem prop="wh" label="宽高:">
        <RadioGroup v-model="addLive.whId" @on-change="addChangeWH" type="button" >
            <Radio  style="margin:5px;border-left:1px solid #dddee1" :label="item.id" v-for="item in whList" :value="item.id" :key="item.id" >
                {{(item.width==0||item.height==0)?'原始分辨率':item.width+'X'+item.height}}
            </Radio>
        </RadioGroup>
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
           width="800px"
    >
      <Form ref="updateLive" :model="updateLive" :label-width="100" label-position="right"  :rules="updateLiveRules">
        <FormItem prop="sourceurl" label="选择来源:">
          <Select v-model="updateLive.sourceurlId" @on-change="selectUpdateSourceurl" transfer size="large" >
              <Option v-for="item in sourceurlList" :value="item.sourceurlId" :key="item.sourceurlId">{{ item.name+"   "+item.url }}</Option>
          </Select>
        </FormItem>
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
        <FormItem prop="playUrl" label="播放url:">
          <Input type="text" v-model="updateLive.playUrl" placeholder="播放url">
          </Input>
        </FormItem>
         <FormItem prop="model" label="模式:">
        <RadioGroup v-model="updateLive.model"  type="button" >
            <Radio  style="margin:5px;border-left:1px solid #dddee1" :label="item.id" v-for="item in modelList" :value="item.id" :key="item.id" >
                {{item.value}}
            </Radio>
        </RadioGroup>
        </FormItem>
        <FormItem prop="wh" label="宽高:">
        <RadioGroup v-model="updateLive.whId" @on-change="updateChangeWH" type="button" >
            <Radio  style="margin:5px;border-left:1px solid #dddee1" :label="item.id" v-for="item in whList" :value="item.id" :key="item.id" >
                {{(item.width==0||item.height==0)?'原始分辨率':item.width+'X'+item.height}}
            </Radio>
        </RadioGroup>
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
         //状态
      statusList:[
        {id:1,value:'直播中'},
        {id:2,value:'停止'},
        {id:3,value:'异常停止'}
        ],
         //模式，1编码解码，2直接转流,3音频转acc
      modelList:[
        {id:1,value:'编码解码'},
        {id:2,value:'直接转流'},
        {id:3,value:'音频转acc'}
        ],
        //宽高 1280X720 850x480 720X404
        whList:[
          {id:1,width:0,height:0},
          {id:2,width:1980,height:1080},
          {id:3,width:1280,height:720},
          {id:4,width:850,height:480},
          {id:5,width:720,height:420},
        ],
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
        whId:1,
        model:2,
        width:0,
        height:0,
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
      sourceurlList: [],
      liveList: [],
      tempLiveList:[],//临时数据
      setinterval:null,//定时任务
	    liveColumns: [
         {
          type: 'selection',
          width:50,
          align: 'center',
          fixed:'left'
        },
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
          title:'播放url',
          minWidth:100,
        	key:'playUrl',
          align:'center'
        },
        {
          title:'码率',
          minWidth:120,
        	key:'videoBitrate',
          align:'center',
            render: (h, params) => {
              return   h('span',{
                        attrs: {
                          id: 'videoBitrate'+params.row.liveId,
                        }},params.row.videoBitrate);
            }
        },
        {
          title:'时长',
          minWidth:120,
        	key:'duration',
          align:'center',
            render: (h, params) => {
              return   h('span',{
                        attrs: {
                          id: 'duration'+params.row.liveId,
                        }},params.row.duration);
            }
        },
        {
          title:'宽*高',
          minWidth:100,
        	key:'width',
          align:'center',
          render: (h, params) => {
            let whvalue="";
            if(params.row.width==0||params.row.height==0){
              whvalue="原始分辨率";
            }else{
              whvalue=params.row.width+" X "+params.row.height
            }
            return  h('span',whvalue);
            }
        },
        {
          title:'模式',
          minWidth:100,
        	key:'model',
          align:'center',
          render: (h, params) => {
            let modelvalue="";
            this.modelList.forEach(element => {
              if(element.id==params.row.model){
                modelvalue=element.value;
              }
            });
            return h('span',modelvalue)
            }
            },
        {
          title:'状态',
          minWidth:100,
        	key:'status',
          align:'center',
          render: (h, params) => {
            let statusvalue="";
            this.statusList.forEach(element => {
              if(element.id==params.row.status){
                statusvalue=element.value;
              }
            });
          var button;
          if(params.row.status==1){
           button= h('Button', {
                        props: {
                          type: 'error',
                          size: 'small'
                        },
                        on: {
                          click: () => {
                            this.changeStatus(params.row,2);
                          }
                        }
                      }, '停止')
          }else if(params.row.status==2){
           button= h('Button', {
                  props: {
                    type: 'success',
                    size: 'small'
                  },
                  on: {
                    click: () => {
                      this.changeStatus(params.row,1);
                    }
                  }
                }, '开始')
          }

             return  h('span',[statusvalue,
              button
             ]);
          }
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
    //增加宽高 
    addChangeWH(d){
      this.whList.forEach(e=>{
        if(e.id==d){
          this.addLive.width=e.width
          this.addLive.height=e.height
        }
      })
    },
    //修改宽高
    updateChangeWH(d){
      this.whList.forEach(e=>{
        if(e.id==d){
          this.updateLive.width=e.width
          this.updateLive.height=e.height
        }
      })
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
    //选择增加的sourceurl
    selectAddSourceurl(d){
        this.sourceurlList.forEach(e=>{
          if(e.sourceurlId==d){
            this.addLive.name=e.name
            this.addLive.sourceUrl=e.url
          }
        })
      
    },
    //选择修改的sourceurl
    selectUpdateSourceurl(d){
        this.sourceurlList.forEach(e=>{
          if(e.sourceurlId==d){
            this.updateLive.name=e.name
            this.updateLive.sourceUrl=e.url
          }
        })
      
    },
    //获取来源url列表
   getSourceurlList () {
     /**
     * 获取列表
     * $this  vue组件
     * p.countUrl 数量url
     * p.listUrl 列表url
     * p.data 返回列表
     */
      this.params.pageSize=1000000;
     this.axiosbusiness.getList(this,{
       countUrl:'/sourceurl/count',
       listUrl:'/sourceurl/list',
       data:'sourceurlList',
       success:()=>{
           this.params.pageSize=10;
           this.selectPage(JSON.parse(this.$route.params.pathParams).currentPage)
       }
     },this.params)
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
    this.params.type=1
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
    this.addLive.type=1
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
         success:()=>{
           this.whList.forEach(e=>{
             if(this.updateLive.width==e.width && this.updateLive.height==e.height){
               this.updateLive.whId=e.id
             }
           })
         }
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
    },
    //切换状态
    changeStatus(params,status){
      let p="?accountId="+this.business.getAccount().accountId;
        p+="&liveId="+params.liveId;
        p+="&status="+status;
      var title=status==1?'开始直播':'停止直播';
      var content=status==1?'确认开始直播吗？':'确认停止直播吗？';

       this.$Modal.confirm({
            title: title,
            content: content,
            onOk: () => {
              this.axiosbusiness.get(this,{
                url:'/live/changeStatus'+p,
                success:(d)=>{
                   this.$Message.success(title+'成功');
                   params.status=status
                }
              });
         },
      onCancel: () => {
         this.$Message.info('取消');
      }
       });
    },
    //全部开启
    startAllLive(){
 let p="?accountId="+this.business.getAccount().accountId;
        p+="&liveIds=all";
        p+="&type=1";
        p+="&status=1";//1直播中
         this.axiosbusiness.get(this,{
                url:'/live/changeStatusBatch'+p,
                success:(d)=>{
                   this.$Message.success('全部开启成功');
                   this.getList()
                }
              });
    },
    //批量开启
    startBatchLive(){
      var als=this.$refs.table.getSelection();
      if(als.length<=0){
        this.$Message.error("最少选一个")
        return;
      }
      var liveIds="";
      als.forEach(e=>{
        liveIds+=e.liveId+",";
      })
       let p="?accountId="+this.business.getAccount().accountId;
        p+="&liveIds="+liveIds;
        p+="&status=1";//1直播中
         this.axiosbusiness.get(this,{
                url:'/live/changeStatusBatch'+p,
                success:(d)=>{
                   this.$Message.success('开始成功');
                   this.getList()
                }
              });
    },
    //全部停止
    stopAllLive(){
      let p="?accountId="+this.business.getAccount().accountId;
        p+="&liveIds=all";
        p+="&type=1";
        p+="&status=2";//2停止
         this.axiosbusiness.get(this,{
                url:'/live/changeStatusBatch'+p,
                success:(d)=>{
                   this.$Message.success('全部停止成功');
                   this.getList()
                }
              });

    },
    //批量停止
    stopBatchLive(){
      var als=this.$refs.table.getSelection();
      if(als.length<=0){
        this.$Message.error("最少选一个")
        return;
      }
      var liveIds="";
      als.forEach(e=>{
        liveIds+=e.liveId+",";
      })
       let p="?accountId="+this.business.getAccount().accountId;
        p+="&liveIds="+liveIds;
        p+="&status=2";//2停止
         this.axiosbusiness.get(this,{
                url:'/live/changeStatusBatch'+p,
                success:(d)=>{
                   this.$Message.success('停止成功');
                   this.getList()
                }
              });
    },
  },
     watch: {
    //当前页面参数修改动态启动
      $route (to,from){
       this.getSourceurlList();
      }
    }, 
  created () {
    this.getSourceurlList();
    
    //this.getList();
  
    this.setinterval=setInterval(()=>{
      this.params.currentPage=JSON.parse(this.$route.params.pathParams).currentPage;
      this.params.pageNum = (this.params.currentPage-1)*this.params.pageSize+this.params.startNum;
      /**
     * 获取列表
     * $this  vue组件
     * p.countUrl 数量url
     * p.listUrl 列表url
     * p.data 返回列表
     */
     this.axiosbusiness.getList(this,{
       countUrl:'/live/count',
       listUrl:'/live/list',
       data:'tempLiveList',//放入临时数据
       success:()=>{
           this.tempLiveList.forEach(te=>{
             document.querySelector("#duration"+te.liveId).innerHTML=te.duration;
               //e.duration=te.duration;
             document.querySelector("#videoBitrate"+te.liveId).innerHTML=te.videoBitrate;
              //e.videoBitrate=te.videoBitrate;
            })
        
       }
     },this.params)
    },3000)
  },
  destroyed(){
     clearInterval( this.setinterval)
  }
}
</script>
