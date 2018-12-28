<!--积分管理 -->
<template>
    <div class="body-wrap">
      <Table border  height="500" :columns='integralColumns' :data='integralList' ref='table' size="small"></Table>
        <div style='display: inline-block;float: right; margin-top:10px;'>
        <Page style='margin-right:10px;'  @on-page-size-change="onPageSizeChange" show-sizer :page-size-opts='params.pageSizeOpts' :total='params.total' :pageSize='params.pageSize' ref='page' :show-total='true'   @on-change='selectPage' show-elevator ></Page>
      </div>
    </div>
    
</template>
<script>
export default {
  name: 'Integral',
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
	    integralList: [],
	    integralColumns: [
        {
          title: '序号',
          align:'center',
          render: (h, params) => {
            return h('span', params.index
            +(this.params.currentPage-1)*this.params.pageSize+this.params.startNum);
          }
        },
        {
          title: '积分id',
          key: 'integralId',
          align:'center'
        },
        {
        	title:'剩余积分',
            key:'integral',
            align:'center'
        },
        {
        	title:'充值积分',
            key:'recharge',
            align:'center'
        },
        {
        	title:'消费金额',
            key:'consume',
            align:'center'
        },
        {
        	title:'赠送积分',
            key:'baseProfit',
            align:'center'
        },
        {
          title:'创建时间',
          key:'createDate',
          sortable: true,
          align:'center'
        },
        {
          title:'修改时间',
          key:'updateDate',
          sortable: true,
          align:'center'
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
       accountId:JSON.parse(this.$route.params.pathParams).accountId,
     })
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
     * p.list 返回列表
     */
     this.params.pageSize=pageSize||this.params.pageSize
    this.params.accountId=JSON.parse(this.$route.params.pathParams).accountId
     this.axiosbusiness.getList(this,{
       countUrl:'/integral/count',
       listUrl:'/integral/list',
       data:'integralList'
     },this.params)
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
  },
  mounted () {
  }
}
</script>