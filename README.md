# 【BytecodeScreen】
本工具设计初衷是由前阵子的beichen师傅挖掘的CS的RCE漏洞，因为刚毕业还在适应新的工作，上个月就一直工作很忙，产品果然没我想的这么好做。终于在国庆假期，可以好好的学习一下主管的这篇文章[https://mp.weixin.qq.com/s/l5e2p_WtYSCYYhYE0lzRdQ](https://mp.weixin.qq.com/s/l5e2p_WtYSCYYhYE0lzRdQ)，看到这个部分的时候有了一些思考![image.png](https://cdn.nlark.com/yuque/0/2022/png/25851247/1665928648372-ca485d99-7cec-42ef-85ea-0befa86ee120.png#clientId=u093b1d65-1a51-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=196&id=u9d5dc17b&margin=%5Bobject%20Object%5D&name=image.png&originHeight=245&originWidth=701&originalType=binary&ratio=1&rotation=0&showTitle=false&size=50123&status=done&style=none&taskId=ube076e0d-1b77-4695-8211-091f2132b22&title=&width=560.8)

这里是主管总结的符合此情况的类的要求。找了一遍网上的工具（可能是我找的不够仔细并且IDEA用的不够熟练），我第一时间没有找到可以帮我快速筛选的工具，昨天看了pang0lin师傅的文章，发现好像是存在这样的情况。
之前跟着threedr3am,Longgofo,4ra1n,su18等很多师傅的文章学习了GadgetInspector代码和原理相关的知识，由此借鉴了很多的代码从而完成了一款基于ASM来获取字节码信息筛选的工具来应对此等情况。
项目使用展示：
```python
D:\Desktop\BytecodeScreen_jar>java -jar BytecodeScreen.jar -h
  ____        _                     _       _____
 |  _ \      | |                   | |     / ____|
 | |_) |_   _| |_ ___  ___ ___   __| | ___| (___   ___ _ __ ___  ___ _ __
 |  _ <| | | | __/ _ \/ __/ _ \ / _` |/ _ \\___ \ / __| '__/ _ \/ _ \ '_ \
 | |_) | |_| | ||  __/ (_| (_) | (_| |  __/____) | (__| | |  __/  __/ | | |
 |____/ \__, |\__\___|\___\___/ \__,_|\___|_____/ \___|_|  \___|\___|_| |_|
         __/ |
        |___/
                                                                       ---Author 0xrumble

=======================================================================================================================
Usage: <main class> [options]
  Options:
    --debug
      make debug
      Default: false
    -h, --help
      Help Info
    -i, --interface
      JAVA InterFace Name
    --jar
      use base rt.jar
      Default: false
    -m, --method
      JAVA Method Name
    -p, --paramter
      JAVA Method Paramter Type
    -r, --return
      JAVA Method Return Paramter Type
    --static
      try do static
      Default: false
    -s, --superclass
      JAVA SuperClass
    -j, --targetpath
      JAVA Target PATH
```
# 参数介绍
## 基础参数
### 参数使用
```python
-h --help: 输出help页面属性。
--debug:   开启debug属性。
-j --targetpath : 
   1.后面跟一个文件夹路径，会读取这个文件夹及子文件夹。例如：
							-j D:\Desktop\testts\target\classes
   2.后面跟一个jar文件路径，会读取这个文件夹及子文件夹。例如：
							-j D:\Desktop\testts\out\artifacts\testts_jar\testts.jar
   3.后面跟一个txt文件路径，会读取这个txt文件内的内容。例如：
                        	-j D:\Desktop\123.txt  
							   内容为（每行一个上面的路径形式）：
								 D:\Desktop\testts\out\artifacts\testts_jar\testts.jar
								 D:\Desktop\testts\target\classes
```
## 方法参数
### 参数分类：
#### 1.基本类型
int,byte,char,double,float,long,short,boolean    例如：{(int)}  {(byte)}
![image.png](https://cdn.nlark.com/yuque/0/2022/png/25851247/1666072867907-12d8085e-f5dd-4f1a-b837-7d984264d4e1.png#clientId=u999e8163-3235-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=74&id=u2fdbc47c&margin=%5Bobject%20Object%5D&name=image.png&originHeight=92&originWidth=1149&originalType=binary&ratio=1&rotation=0&showTitle=false&size=12692&status=done&style=none&taskId=u3730b874-854c-4ec6-89d1-557b05a2b10&title=&width=919.2)
#### 2.特殊类型
void   例如：{(void)}
![image.png](https://cdn.nlark.com/yuque/0/2022/png/25851247/1666073710905-0594afb2-2486-4b44-9bbb-5eb321771bf6.png#clientId=u999e8163-3235-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=70&id=u578297b6&margin=%5Bobject%20Object%5D&name=image.png&originHeight=88&originWidth=631&originalType=binary&ratio=1&rotation=0&showTitle=false&size=7921&status=done&style=none&taskId=u3dbe8689-aa0b-4faa-b32b-b0fc5580b69&title=&width=504.8)
#### 3.对象类型
普通对象：全限定类名。例如：{(java.lang.String)}     ,    {( java.lang.Boolean)}
数组对象：全限定类名+[]  例如：一维数组：{(java.lang.String[])}，二维数组：{{java.lang.String[][]}}
![image.png](https://cdn.nlark.com/yuque/0/2022/png/25851247/1666072376549-55180b24-5c7e-40ea-a02c-f57db58254c4.png#clientId=u999e8163-3235-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=225&id=ufc7c8c2a&margin=%5Bobject%20Object%5D&name=image.png&originHeight=281&originWidth=667&originalType=binary&ratio=1&rotation=0&showTitle=false&size=28169&status=done&style=none&taskId=ub62833e9-6570-4ce1-97ba-ddf65f847c3&title=&width=533.6)
#### 4.内部类
普通内部对象变量：全限定类名+$+内部类名。例如：{(infra.config.Configuration$Module)}
数组内部对象变量：全限定类名+$+内部类名+[]  例如：{(infra.config.Configuration$Module[])}
![image.png](https://cdn.nlark.com/yuque/0/2022/png/25851247/1666024362056-b73ed4ae-073b-4b10-9a25-ed08d6a9ec58.png#clientId=u80dffb66-fee4-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=217&id=u8d31dfbc&margin=%5Bobject%20Object%5D&name=image.png&originHeight=271&originWidth=1047&originalType=binary&ratio=1&rotation=0&showTitle=false&size=49509&status=done&style=none&taskId=u496934e5-54d6-423b-aed0-f5c3193a5b3&title=&width=837.6)
### 参数使用
```python
-i, --interface   指定实现的全路径接口名称（多个用,分割） 例如：
                             -i org.example.Interface3,org.example.Interface2 
-s, --superclass  指定继承的全路径父类名称,例如：
                             -s org.example.class1

-m, --method      指定方法名称，例如：
                        (确定的传入参数)   -m {(setpar1)}
                       （不确定的传入参数） -m {(set*)} 

-p, --paramter    指定全路径的传入参数名称，例如：
                             (确定的传入参数，多个用,分割)   -p {(java.lang.String)}
                             (不确定的传入参数)             -p {(*)}        	
                             (没有传入参数)                 -p {(void)}
-r, --return      指定全路径的返回值名称，例如：
                             (确定的返回值)                 -r {(java.lang.String)}
                             (不确定的返回值）              -r {(*)} 
                             (没有传入参数)                 -r {(void)}

--static          扫描static方法，默认不查找static方法
--jar             扫描默认jre路径下的rt.jar包
```
# 工具使用
## 例子1：无参构造函数
1.规定方法名称（构造方法用<init>）
2.参数确定（为空）
3.返回值确定（为空）
```python
java -jar BytecodeScreen.jar -j D:\Desktop\123.txt -m "{(<init>)}"  -p {(void)} -r {(void)}
```
![image.png](https://cdn.nlark.com/yuque/0/2022/png/25851247/1666078295492-0ec91113-e52b-40c8-b5f6-217a93fe1775.png#clientId=u999e8163-3235-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=224&id=u027d6e7c&margin=%5Bobject%20Object%5D&name=image.png&originHeight=280&originWidth=1301&originalType=binary&ratio=1&rotation=0&showTitle=false&size=10281&status=done&style=none&taskId=ud6fd03e4-eafb-4518-9e6a-2cc26578c17&title=&width=1040.8)
## 例子2：查找特定方法
1.查找set方法
2.传入参数不限
3.返回值为空
```python
java -jar BytecodeScreen.jar -j D:\Desktop\hcc\123.txt -m {(set*)}  -p {(*)} -r {(void)}
```
![image.png](https://cdn.nlark.com/yuque/0/2022/png/25851247/1666078037904-c4cc5bed-e871-41ee-acf6-416ba44092a4.png#clientId=u999e8163-3235-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=328&id=ubbddd460&margin=%5Bobject%20Object%5D&name=image.png&originHeight=410&originWidth=1382&originalType=binary&ratio=1&rotation=0&showTitle=false&size=19754&status=done&style=none&taskId=ub132f57f-adf6-4fc5-acd7-f0d3769e313&title=&width=1105.6)
## 例子3：查找特定类方法
1.继承xxxxx
2.实现xxxxx的接口
3.方法名包含xxxx
4.参数包含xxxx
5.返回值是xxxx
```python
-j D:\Desktop\123.txt -m "{(class*)}{(<init>)}"  -p {(java.util.List,java.lang.String)}{(void)} -r {(java.lang.String)}{(void)} -s org.example.class8 -i org.example.interfaces1,org.example.Interface2
```
![image.png](https://cdn.nlark.com/yuque/0/2022/png/25851247/1666080524131-95391dcb-4a07-4d0a-97b9-f5a3135307be.png#clientId=u999e8163-3235-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=135&id=u17571564&margin=%5Bobject%20Object%5D&name=image.png&originHeight=264&originWidth=1473&originalType=binary&ratio=1&rotation=0&showTitle=false&size=9771&status=done&style=none&taskId=ud7f7f977-b19c-41d5-86c1-14fd798c21b&title=&width=753)
## 例子4（这里就只扫描rt.jar）：
![image.png](https://cdn.nlark.com/yuque/0/2022/png/25851247/1665928648372-ca485d99-7cec-42ef-85ea-0befa86ee120.png#clientId=u093b1d65-1a51-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=196&id=G740s&margin=%5Bobject%20Object%5D&name=image.png&originHeight=245&originWidth=701&originalType=binary&ratio=1&rotation=0&showTitle=false&size=50123&status=done&style=none&taskId=ube076e0d-1b77-4695-8211-091f2132b22&title=&width=560.8)
```python
java -jar BytecodeScreen.jar -j D:\Desktop\123.txt -m "{(<init>)}{(set*)}" -p {(void)}{(java.lang.String)} -r {(void)}{(void)}  -s java.awt.Component
```
![image.png](https://cdn.nlark.com/yuque/0/2022/png/25851247/1666109233463-5078d781-2d63-4f3b-9a85-b06acb159c58.png#clientId=u999e8163-3235-4&crop=0&crop=0&crop=1&crop=1&from=paste&height=413&id=u0a79e4a8&margin=%5Bobject%20Object%5D&name=image.png&originHeight=516&originWidth=1203&originalType=binary&ratio=1&rotation=0&showTitle=false&size=47988&status=done&style=none&taskId=ue473dd9a-eb06-4f62-be11-e6053636043&title=&width=962.4)
具体使用一共18种情况：
![image](https://user-images.githubusercontent.com/115286245/196492367-6fb3164a-8281-4ba6-853c-cf24e238a010.png)
# 总结
希望这个工具可以帮助到师傅们在工作学习中减少在不必要的方面浪费时间。也希望师傅们可以多挖漏洞，快速变强。如果有什么问题或者改进的想法，请多多提需求。
