import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
public class KeywordCounter {

    public static void main(String args[]) throws Exception
    {
        FibonacciHeap fh = new FibonacciHeap();
        HashMap<String,FibonacciHeap.Link_Node> map=new HashMap<String,FibonacciHeap.Link_Node>();                  //HashMap to store all the Fibonacci nodes and their frequency
        List<Record> lst= new ArrayList<Record>();                                      //List to store all the extracted nodes so that they can be re-inserted
        try {
            FileInputStream fstream = new FileInputStream(args[0]);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            File file = new File("output_file.txt");

            if (file.exists()) {
                file.delete();                                                      //delete the file if it already exists
            }

            PrintWriter writer = new PrintWriter("output_file.txt", "UTF-8");       //write to the output file
            while ((strLine = br.readLine()) != null) {
                String[] tokens = strLine.split(" ");
                if(tokens[0].toLowerCase().equals("stop"))                                      //end when you find stop
                    break;
                if(tokens[0].charAt(0)=='$')                                                    //process record
                {
                    Record record = new Record(tokens[0], Integer.parseInt(tokens[1]));
                    if(map.get(record.word)==null)
                    {
                        FibonacciHeap.Link_Node cnode=fh.insert(record);
                        map.put(record.word,cnode);
                    }
                    else
                        fh.Increase_Key(map.get(record.word),map.get(record.word).node.record.freq+record.freq);//Increase the frequency value if the word already exists
                }
                else                                                                   //process query
                {
                    for(int i=1;i<=Integer.parseInt(tokens[0]);i++) {
                        // System.out.println(i+"th extract max");
                        FibonacciHeap.Link_Node node = fh.extract_Max();//node.node.record.word

                        if(node==null){
                          writer.print("There is no "+i+"th max");
                        }
                        else
                        {
                            writer.print(node.node.record.word.substring(1));
                            map.remove(node.node.record.word);                        //removing the extracted node from the map so that it can be re-inserted later
                            lst.add(node.node.record);
                        }
                        if(i < Integer.parseInt(tokens[0]))
                            writer.print(",");
                    }
                    writer.print("\n");

                    for(Record record:lst)                                              //re-inserting extracted nodes
                    {
                        FibonacciHeap.Link_Node cnode=fh.insert(record);
                        map.put(record.word,cnode);
                    }
                    lst.clear();
                }
            }               // while ends
            writer.close();
            in.close();}    // try block ends
        catch (Exception e) {  System.err.println("Error: " + e.getMessage());       }
    }
}

/* class to store word and frequency*/
class Record {
    String word;
    int freq;

    public Record(String word, int freq) {
        this.word = word;
        this.freq = freq;
    }
}

/* Fibonacci Node **/
class FNode
{
    Record record;
    FNode parent;
    FNode next;
    FNode child;
    int degree;
    boolean childCut;

    //Constructor//
    FNode(Record record){
        this.record = record;
    };
}

class FibonacciHeap
{
    FNode root;
    HashMap<Integer, Link_Node> hmap = new HashMap<>();             //hashmap to store all the nodes and their degree while extracting max

    /** Constructor **/
    public FibonacciHeap()
    {
        root = null;
    }

    public class Link_Node {             //to link all the root nodes in doubly and circular linked list
        FNode node;
        Link_Node next;
        Link_Node pre;
        Link_Node(FNode node){
            this.node = node;
        }
    }

    //pointer to point to the maximum node of the heap//
    Link_Node max_node;

    /** Function to insert **/
    public Link_Node insert(Record record) {
        Link_Node new_node=new Link_Node(new FNode(record));
        if (max_node == null)                                   //if there is no maxnode yet, create one
        {
            max_node = new_node;
            return max_node;
        }

        else {
            Link_Node next = max_node.next;                      //creating doubly linked list
            max_node.next=new_node;
            max_node.next.next = next;
            max_node.next.pre = max_node;

            /*converting the doubly linked list to circular doubly linked list by making the two end pointers point to each other*/
            if (next != null) next.pre = max_node.next;
            else {
                next = max_node.next;
                next.next = max_node;
                max_node.pre = next;
            }
        }
        //updating max pointer if required
        if(max_node.next != null && max_node.next.node.record.freq > max_node.node.record.freq)
        {
            max_node = max_node.next;
            return max_node;
        }
        return new_node;
    }

    /*function to return next maximum*/
    /* Check all the root nodes starting from the node next to received head till you reach the head node again*/
    private Link_Node getNextMax(Link_Node head){
        if(head == null )
            return null;
        if(head.next==null)
            return head;
        Link_Node tmp = head;                               //tmp node helps preserve the information of the received head node
        tmp=tmp.next;
        if(tmp==max_node)
            return tmp.pre;
        Link_Node max=new Link_Node(new FNode(new Record("dummy",0))); //created dummy max node of freq 0 so that initial comparison is done against it

        while(tmp != head){
            if(tmp.node.record.freq > max.node.record.freq && tmp.node.record.freq <= max_node.node.record.freq && tmp!=max_node) max = tmp;
            tmp=tmp.next;
        }
        if(tmp.node.record.freq > max.node.record.freq && tmp.node.record.freq <= max_node.node.record.freq && tmp!=max_node) max = tmp;
        return max;
    }

    /* Two cases: a) Degree is 0: three cases:
                                  (i) there is no other node in the heap
                                 (ii) There is only one other parent node in the heap
                                 (iii) There are various other parent nodes in the heap-- pairwise combining needed in this one
                  b) Degree is greater than 0
     */
    public Link_Node extract_Max()
    {
        if(max_node==null) return null;
        else
        {
            Link_Node max= max_node;
            if(max_node.node.degree==0)
            {
                if(max_node.next==null) {
                    max_node=null;
                    return max;
                }
                else if(max_node.pre==max_node.next)
                {
                    max_node=max_node.next;
                    max_node.pre=null;
                    max_node.next=null;
                    return max;
                }
                else
                {
                    max_node.pre.next=max_node.next;
                    max_node.next.pre=max_node.pre;
                    max_node=getNextMax(max_node.next);
                }
            }
            else
            {   Link_Node next;
                Link_Node pre;
                if(max_node.next==null)
                {
                    next = null;
                    pre = null;
                }
                else
                {
                    next = max_node.next;            //bringing all the child nodes in the parent link list of all parent nodes
                    pre = max_node.pre;
                }

                int degree = max_node.node.degree;
                FNode fnode = max_node.node.child;
                max_node.node.child.parent=null;
                max_node.node.child=null;
                while(degree>0)
                {
                    /* bringing all the child nodes in the parent link list of all parent nodes*/
                    Link_Node Node=new Link_Node(fnode);
                    if(pre==null && next==null)
                    {
                        Node.next=null;
                        Node.pre=null;
                    }
                    else if(pre==null)
                    {
                        Node.pre=next;
                        Node.next=next;
                        next.pre=Node;
                        next.next=Node;
                    }
                    else
                    {   Node.next=next;
                        next.pre=Node;
                        Node.pre=pre;
                        pre.next=Node;
                    }
                    Node.node.childCut=false;  //setting the child cut value to false for all the child nodes of the removed max node once they become the root nodes post removal
                    fnode=fnode.next;
                    pre=Node.pre;
                    next=Node;
                    degree--;
                }

                max_node = getNextMax(next);                        //getting the next max
            }
            /* finding out the nodes to be melded*/
            Link_Node n=max_node;                                   //start checking the node degree for all the nodes starting from the maxnode
            if(n.next != null)
            {
                Link_Node tmp=n;
                while (n != null && n.next != tmp) {
                    Link_Node Node = n;
                    /*check if there exists a node with same degree on the hasmap. If yes, then merge the two*/
                    if(hmap.containsKey(n.node.degree) && hmap.get(n.node.degree).node.record.word!=n.node.record.word) {
                        int old_degree = n.node.degree;
                        Node = meld(n, hmap.get(n.node.degree));
                        hmap.remove(old_degree); //removing the old degree node to add the new one,since there will no node of old degree in the heap post melding
                        hmap.put(Node.node.degree, Node);
                    }

                    n = Node;
                    n = n.next;
                }

                Link_Node Node=n;
                if(hmap.containsKey(n.node.degree))
                    Node=meld(n,hmap.get(n.node.degree));
                hmap.put(Node.node.degree,Node);
            }

            hmap.clear();                   //clearing the hasMap so that it can be filled fom scratch for next extractmax operation
            return max;
        }

    }

    public Link_Node meld(Link_Node parent, Link_Node new_child)
    {
        if(parent.node.record.freq<new_child.node.record.freq)
        {
            Link_Node temp=parent;
            parent=new_child;
            new_child=temp;
        }

        /*detach the new child node from the root link list*/
        if(new_child.pre!=null)
        {
            if(new_child.pre == new_child.next){
                new_child.pre.next = null;
                new_child.pre.pre = null;
            } else{
                new_child.pre.next = new_child.next;
                new_child.next.pre = new_child.pre;
            }}
        new_child.pre=new_child.next=null;
        hmap.remove(new_child);                             //it needs to be removed since it is no longer one of the root nodes
        new_child.node.parent=parent.node;
        new_child.node.childCut=false;                      //child Cut needs to be set to false once a node becomes the child of another parent
        if(parent.node.child==null)                         //when newly melded child is the only child of the parent node
        {
            parent.node.child=new_child.node;
            new_child.node.next=null;
        }

        else
        {
            new_child.node.next=null;
            parent.node.child.next=new_child.node;
        }

        parent.node.degree++;                               //incrementing degree of parent node
        hmap.remove(new_child.node.degree);
        if(hmap.containsKey(parent.node.degree))            //meld again if the hmap contains another node of same degree post melding
            parent = meld(parent,hmap.get(parent.node.degree));

        return parent;
    }

    public void Increase_Key(Link_Node Node, int newValue){
        /*Increasing the node value and checking for greater than parent violation*/
        Node.node.record.freq = newValue;
        if(Node.node.parent == null)
        {
            if(Node.node.record.freq>max_node.node.record.freq)
                max_node=Node;
            return;
        }
        if(Node.node.parent != null && Node.node.record.freq > Node.node.parent.record.freq)
            ParentViolationFound(Node.node,true);//check its value compared to its parent
    }

    public void ParentViolationFound(FNode node,boolean increased_node){
        if(node == node.parent.child) node.parent.child = node.next;        //change child pointer, if required
        else {
            FNode n = node.parent.child;                                    //remove the node from the list of child nodes
            while(n.next != node) n = n.next;
            n.next = node.next;
            node.next=null;
        }

        if(node.parent.childCut!=true)
        {
            node.parent.childCut=true;
            if(node.next==null)
                node.parent.degree=node.parent.degree-1;                    //reducing the degree by one since the new value child node is being removed
            node.parent=null;
        }
        else                                                                    //check cascading cut impact
        {
            if(node.parent.parent!=null)
                ParentViolationFound(node.parent,false);
        }

        Link_Node Node=new Link_Node(node);
        if(max_node.next==null)                                                 //updating the root Link list when only two nodes exist
        {
            Node.next=max_node;
            Node.pre=max_node;
            max_node.next=Node;
            max_node.pre=Node;

        }
        else                                                           //updating the root link list when more than two nodes exist
        {
            Node.next=max_node.next;
            max_node.next.pre=Node;
            max_node.next=Node;
            Node.pre=max_node;
        }
        if(increased_node==true){                                               //updating the max_pointer, if required
            if(Node.node.record.freq>max_node.node.record.freq)
                max_node=Node;
        }

    }
}
