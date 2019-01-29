package com.nieyue.util;

import com.mysql.jdbc.Connection;
import com.nieyue.bean.Account;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * jdbc
 */
public class MyJDBC {
    String driver;
    String url;
    String username;
    String password;

    public MyJDBC(String driver, String url, String username, String password){
        this.driver=driver;
        this.url=url;
        this.username=username;
        this.password=password;
    }

    /**
     * 加载初始化。获取连接
     * @return
     */
    private  Connection getConn() {
        Connection conn = null;
        try {
            Class.forName(this.driver); //classLoader,加载对应驱动
            conn = (Connection) DriverManager.getConnection(this.url, this.username, this.password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 关闭资源
     * @param connection
     * @param PreparedStatement
     * @param resultSet
     */
    public void close(Connection connection, PreparedStatement PreparedStatement, ResultSet resultSet){
        try {
            if(connection!=null){
                connection.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        try {
            if(PreparedStatement!=null){
                PreparedStatement.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        try {
            if(resultSet!=null){
                resultSet.close();
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * 通用增删改查
     * @param sql
     * @return
     */
    public int CUD(String sql, List<Object> parameters){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int result = 0;
        try {
            conn = getConn();
            ps = conn.prepareStatement(sql);
            setParameter(ps,parameters);
            result = ps.executeUpdate();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.getMessage());
        }finally{
            close(conn,ps,rs);
        }
        return result;
    }
    /**
     * 设置参数
     * @param parameters 参数集
     * @throws SQLException 抛出SQL异常
     */
    private void setParameter( PreparedStatement ps,List<Object> parameters)
        throws SQLException {
            if(parameters!=null && parameters.size()>0)
            {
                for(int i = 0;i<parameters.size();i++)
                {
                    ps.setObject(i+1, parameters.get(i));
                }
            }
    }

    /**
     *封装数据库的查操作
     * @param c 反射类的对象
     * @param sql 操作的查询SQL语句
     * @param parameters 参数集,调用时无则写null
     * @return list 集合
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws SQLException
     * @throws InstantiationException
     */
    public  List query(Class c,String sql,List<Object> parameters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SQLException, InstantiationException
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List list = new ArrayList();
        Object o = null;
        conn = getConn();
        ps = conn.prepareStatement(sql);
        setParameter(ps,parameters);
        rs = ps.executeQuery();
        //得到列信息ResultSetMetaDate对象
        ResultSetMetaData rsmd =rs.getMetaData();

        //创建一个String的数组,用来保存所有的列名
        //rsmd.getColumnCount()为当前结果集中的列的总数,所以定义为长度
        String[] cName = new String[rsmd.getColumnCount()];
        for (int i = 0; i < cName.length; i++) {
            cName[i] = rsmd.getColumnName(i + 1);
            //下划线转驼峰；
            cName[i]= MyDom4jUtil.UnderlineToHump(cName[i]);
        }


        //得到反射类中的所有的方法
        Method[] methods=c.getMethods();

        while(rs.next())
        {
            //如果结果集得到了数据,则实例一个对象
            o = c.newInstance();
            for(int i=0;i<cName.length;i++)
            {
                for(Method m:methods)
                {
                    //把从结果集中得到列名前面加上"set",并把第一位设置为大写,加上后面的,成为一个set的名称,
                    //然后用反射得到的方法名与之比较,相同的话则激活此方法
                    if(m.getName().equals("set"+cName[i].substring(0,1).toUpperCase()+cName[i].substring(1)))
                    {
                        //激活得到方法,并设置值
                        m.invoke(o, rs.getObject(i+1));
                    }
                }
            }
            //添加到list集合中
            list.add(o);
        }
        return list;
    }

    public static void  test(){
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/freeride_db";
        String username = "root";
        String p = "liliu";
        MyJDBC myJDBC = new MyJDBC(driver, url, username, p);
        List<Object> parameters=new ArrayList<>();
        parameters.add("的所发生的");
        parameters.add(2);
        parameters.add(new java.util.Date());
        parameters.add(new java.util.Date());
        String add="insert into activation_code_tb (code,is_usered,create_date,update_date) values(?,?,?,?)";
        //String update = "update students set Age='" + student.getAge() + "' where Name='" + student.getName() + "'";
        //String delete = "delete from students where Name='" + name + "'";
        myJDBC.CUD(add,parameters);
    }
    public static void  testQuery(){
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/freeride_db";
        String username = "root";
        String p = "liliu";
        MyJDBC myJDBC = new MyJDBC(driver, url, username, p);
       /* List<Object> parameters=new ArrayList<>();
        parameters.add("123456");
        parameters.add(1);
        parameters.add(new java.util.Date());
        parameters.add(new java.util.Date());*/
        String sql="select * from activation_code_tb";
        List<Account> accounts=new ArrayList<>();
        try {
            accounts=myJDBC.query(Account.class,sql,null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < accounts.size(); i++) {
            System.out.println(accounts.get(i).getIcon());
            System.out.println(accounts.get(i).getPhone());
            System.out.println(accounts.get(i).getCreateDate());
            System.out.println(accounts.get(i).getLoginDate());
            System.out.println("----------------");
        }
    }
    public static void  testcsv(){
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/freeride_db";
        String username = "root";
        String p = "liliu";
        MyJDBC myJDBC = new MyJDBC(driver, url, username, p);
        List<String> dataList= MyCSV.importCsv(new File("E:\\qq\\data\\程序安装材料\\激活码数据.csv"));
        if(dataList!=null && !dataList.isEmpty()){
            for(String data : dataList){
                List<Object> parameters=new ArrayList<>();
                System.out.println(data);
                String[] splits = data.split(",");
                parameters.add(splits[0]);
                parameters.add(splits[1]);
                System.out.println(splits[1]);
                parameters.add(1);
                parameters.add(splits[3]);
                parameters.add(splits[4]);
                String add="insert into activation_code_tb (activation_code_id,code,is_usered,create_date,update_date) values(?,?,?,?,?)";
                int number = myJDBC.CUD(add, parameters);
            }
        }
    }
    public static void main(String[] args) {
        //test();
        //testQuery();
        testcsv();

    }
}
