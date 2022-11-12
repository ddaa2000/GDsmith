package org.example.gdsmith.performance;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TreeNode {
    static Random r = new Random();
    static int nActions=5;    //五个步骤，也就是五个子节点
    static double epsilon =1e-6;

    TreeNode[] children;        //该结点的五个子节点
    public int nVisits;
    public int totValue;     //总的访问次数，总胜负次数

    public boolean isLeaf(){  //是不是下面没有子结点
        return children==null;
    }

    public TreeNode select(){   //按照uct公式计算每个子节点，找出最大值，返回该结点。
        TreeNode selected=null;
        double bestValue = -Double.MAX_VALUE;
        for (TreeNode c:children){      //计算每个孩子的uct的值
            double uctValue =c.totValue/ (c.nVisits+epsilon)+
                    Math.sqrt(Math.log(nVisits+1)/(c.nVisits+epsilon))+r.nextDouble()*epsilon;
            if(uctValue>bestValue){
                selected=c;
                bestValue=uctValue;
            }
        }
        return selected;
    }

    public void expand(){  //扩展当前结点的5个孩子结点
        children=new TreeNode[nActions];  //扩展当前结点的子节点，扩展5个孩子
        for(int i=0;i<nActions;i++){
            children[i]=new TreeNode();  //对于一个类的数组，中间每一个都要进行初始化
        }
    }


    public void selectAction(){  //这里是最关键的函数
        List<TreeNode> visited =new LinkedList<>(); //存储访问路径上面的结点
        TreeNode cur=this; //当前结点
        System.out.print("当前结点为："+cur.totValue+"/"+cur.nVisits+" \n ");
        visited.add(this);
        while(!cur.isLeaf()){  //如果当前结点不是最底层节点
            cur=cur.select();  //往下走，把当前结点设置为uct最大的那个子结点
            visited.add(cur);   //把选择过的结点都加到visited队列里面
            System.out.print("下一级结点是"+cur.totValue+"/"+cur.nVisits+"  ");
        }
        System.out.print("\n");
        cur.expand();         //这里不是很明白为什么要扩展5个
        TreeNode newNode = cur.select();
        visited.add(newNode);
        int value=rollOut();
        for (TreeNode node :visited){    //搜索路径上面的每个结点都要重新更新值
            //对于n个参与者的游戏需要其他的逻辑
            node.updateState(value);
        }
    }

    public int rollOut(){  //随机返回tn节点的胜负，这里可以有更加优化的算法
        return r.nextInt(2);  //该方法的作用是生成一个随机的int值，该值介于[0,n)的区间,这里也就是0或者1
    }

    public void updateState(double value){
        nVisits++;   // 该节点的访问次数+1
        totValue+=value; //该节点的胜利次数+1
    }
    public int arity(){   //返回有几个孩子
        return children==null?0:children.length;
    }

}
