package com.morening.java.learn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


public class Main {

    private static final int[][] offset = {{-1, 0}, {-1, 1}, {0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}};
    private static final int EACH_DIS = 10;

    public static void main(String[] args) throws FileNotFoundException {

        System.setIn(new FileInputStream("input.txt"));
        Scanner sc = new Scanner(System.in);
        int M = sc.nextInt();
        int N = sc.nextInt();
        int[][] map = new int[M+1][N+1];
        int start_x = 0;
        int start_y = 0;
        int end_x = 0;
        int end_y = 0;
        for (int i=0; i<M; i++){
            for (int j=0; j<N; j++){
                map[i][j] = sc.nextInt();
                if (map[i][j] == 1){
                    start_x = i;
                    start_y = j;
                }
                if (map[i][j] == 2){
                    end_x = i;
                    end_y = j;
                }
            }
        }

        BFS(start_x, start_y, end_x, end_y, map, M, N);
        AStarAlgorithm(start_x, start_y, end_x, end_y, map, M, N);

        sc.close();
    }

    private static void BFS(int start_x, int start_y, int end_x, int end_y,
                            int[][] map, int M, int N) {
        int count = 0;
        Node answer = null;
        boolean[][] visited =new boolean[M][N];
        Queue<Node> queue = new LinkedList<>();
        queue.add(new Node(start_x, start_y, 0));
        visited[start_x][start_y] = true;
        while (!queue.isEmpty()){
            count++;
            Node temp = queue.poll();
            if (temp.x == end_x && temp.y == end_y){
                answer = temp;
                break;
            }
            for (int k=0; k<8; k++){
                int offset_x = temp.x + offset[k][0];
                int offset_y = temp.y + offset[k][1];
                if (offset_x >= 0 && offset_x < M && offset_y >= 0 && offset_y < N
                        && isReachable(offset_x, offset_y, map) && !visited[offset_x][offset_y]){
                    int gVal = temp.gVal + getDistance(temp.x, temp.y, offset_x, offset_y);
                    Node nextNode = new Node(offset_x, offset_y, gVal);
                    nextNode.parent = temp;
                    queue.add(nextNode);
                    visited[offset_x][offset_y] = true;
                }
            }
        }

        System.out.println("=== BFS Algorithm ===");
        System.out.println("搜索次数："+count);
        System.out.println("最优解："+answer.gVal);
        printPath(answer);
    }

    /*
     * A* 一种静态路网中求解最短路径最有效的直接搜索方法
     * 步骤如下：
     * 1. 将起点S加入Open队列
     * 2. 将所有S可到达的点加入到Open队列，将S从Open中删除，添加到Close队列
     * 3. 从Open队列中删除f值最小的点Min，并将其加入Close队列
     * 4. 将Min点所有可到达的点加入Open队列，并设这些点的父节点为Min。若某点已经在Open中，则比较其f值，若新路径f值较小，说明从Min走路径更短，更新父节点为Min；否则不更新此节点
     * 5. 循环3，4，知道Open队列出现终点
     *
     * 公式：f(n) = g(n) + h(n)
     * f(n) 是从初始状态经由状态n到目标状态的代价估计，
     * g(n) 是在状态空间中从初始状态到状态n的实际代价，
     * h(n) 是从状态n到目标状态的最佳路径的估计代价。
     */
    private static void AStarAlgorithm(int start_x, int start_y, int end_x, int end_y, int[][] map, int M, int N) {
        int count = 0;
        Node answer = null;

        Node openHead = new Node();
        Node startNode = new Node(start_x, start_y, 0);
        startNode.fVal = 0 + getHVal(start_x, start_y, end_x, end_y);
        insertNode(openHead, startNode);
        Node closeHead = new Node();
        while (openHead.next != openHead){
            count++;
            Node temp = findMinFValNode(openHead);
            if (temp.x == end_x && temp.y == end_y){
                answer = temp;
                break;
            }
            removeNode(temp);
            insertNode(closeHead, temp);
            for (int k=0; k<8; k++){
                int offset_x = temp.x + offset[k][0];
                int offset_y = temp.y + offset[k][1];
                if (offset_x >= 0 && offset_x < M && offset_y >= 0 && offset_y < N
                        && isReachable(offset_x, offset_y, map) && findIn(offset_x, offset_y, closeHead) == null){
                    int gVal = temp.gVal + getDistance(temp.x, temp.y, offset_x, offset_y);
                    int fVal = gVal + getHVal(offset_x, offset_y, end_x, end_y);
                    Node openNode = findIn(offset_x, offset_y, openHead);
                    if (openNode == null){
                        Node nextNode = new Node(offset_x, offset_y, gVal);
                        nextNode.fVal = fVal;
                        nextNode.parent = temp;
                        insertNode(openHead, nextNode);
                    } else {
                        if (openNode.fVal > fVal){
                            openNode.fVal = fVal;
                            openNode.gVal = gVal;
                            openNode.parent = temp;
                        }
                    }
                }
            }
        }

        System.out.println("=== A* Algorithm ===");
        System.out.println("搜索次数："+count);
        System.out.println("最优解："+answer.gVal);
        printPath(answer);
    }

    private static void printPath(Node end) {
        Node cur = end;
        while (cur != null){
            System.out.print(String.format("(%d, %d) <- ", cur.x, cur.y));

            cur = cur.parent;
        }
        System.out.println();
    }

    private static int getHVal(int start_x, int start_y, int end_x, int end_y) {

        return (Math.abs(start_x-end_x)+Math.abs(start_y-end_y))*EACH_DIS;
    }

    private static int getDistance(int start_x, int start_y, int end_x, int end_y) {

        return (int)(Math.sqrt(Math.pow(start_x-end_x, 2) + Math.pow(start_y-end_y, 2))*EACH_DIS);
    }

    private static Node findIn(int x, int y, Node head) {
        Node cur = head.next;
        while (cur != head){
            if (cur.x == x && cur.y == y){
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    private static boolean isReachable(int x, int y, int[][] map) {

        return map[x][y] == 0 || map[x][y] == 2;
    }

    private static Node findMinFValNode(Node head) {
        int minFVal = Integer.MAX_VALUE;
        Node ret = null;
        Node cur = head.next;
        while (cur != head){
            if (minFVal > cur.fVal){
                minFVal = cur.fVal;
                ret = cur;
            }
            cur = cur.next;
        }
        return ret;
    }

    private static void removeNode(Node temp) {
        Node pre = temp.pre;
        Node next = temp.next;
        pre.next = next;
        next.pre = pre;

        temp.next = temp;
        temp.pre = temp;
    }

    /*前出入*/
    private static void insertNode(Node head, Node temp) {
        temp.next = head.next;
        temp.pre = head;
        head.next = temp;
    }

    static class Node{
        int x = 0;
        int y = 0;
        int gVal = 0;
        int fVal = 0;

        Node parent = null;

        Node pre = this;
        Node next = this;

        public Node(){

        }

        public Node(int x, int y, int gVal){
            this.x = x;
            this.y = y;
            this.gVal = gVal;
        }
    }
}