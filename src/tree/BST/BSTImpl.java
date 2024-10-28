package tree.BST;

class Node {
    Node(int val){
        this.val = val;
    };
    int val;
    Node left;
    Node right;
}

class BST {

    private Node root;

    public static void main(String[] args) {
        BST tree = new BST();
        tree.insert(23);
        tree.insert(3);
        tree.insert(34);
        tree.insert(21);
        tree.insert(8);
        tree.insert(56);
        tree.insert(30);
        tree.insert(44);
        tree.insert(99);
        tree.insert(35);
        tree.insert(52);
        tree.insert(27);
        tree.insert(25);
        tree.insert(29);
        tree.insert(32);
        tree.insert(31);
        tree.traverse(tree.root);
        Node temp = tree.search(32);
        System.out.println();
        System.out.println(temp.val);
        tree.remove(34);
        tree.remove(32);
        tree.traverse(tree.root);
    }


    public void remove(int val) {
        remove(this.root, val);
    }

    public Node remove(Node curr, int val) {
        if(curr == null) {
            return null;
        }
        while(curr != null) {
            if(curr.val < val){
                curr.right = remove(curr.right, val);
                return curr;
            }
            else if(curr.val == curr.val) {
                if(curr.left != null && curr.right != null) {
                    Node rightmost = searchRightmost(curr.left);
                    int temp = rightmost.val;
                    curr.left = remove(curr.left, temp);
                    curr.val = temp;
                    return curr;
                }
                else if(curr.left != null) {
                    return curr.left;
                }
                else if(curr.right != null) {
                    return curr.right;
                }
                else {
                    return null;
                }
            }
            else {
                curr.left = remove(curr.left, val);
                return curr;
            }
        }
        return null;
    }


    public Node searchRightmost(Node curr) {
        while(curr.right!=null) {
            curr = curr.right;
        }
        return curr;
    }


    public Node search(int val) {
        Node curr = root;
        while(curr != null) {
            if(curr.val < val){
                curr = curr.right;
            }
            else if(curr.val == val) {
                return curr;
            }
            else {
                curr = curr.left;
            }
        }
        return null;
    }

    public void traverse(Node curr) {
        if(curr == null) {
            return;
        }
        if(curr.left != null) {
            traverse(curr.left);
        }
        System.out.print(curr.val + "  ");
        if(curr.right != null) {
            traverse(curr.right);
        }
    }



    public void insert(int val) {
        Node prev = null;
        Node curr = root;
        while(curr!=null) {
            prev = curr;
            if(curr.val > val) {
                curr = curr.left;
            }
            else if(curr.val == val) {
                return;
            }
            else{
                curr = curr.right;
            }
        }
        curr = new Node(val);
        if(prev == null) {
            root = curr;
        }
        else{
            if(prev.val >= val) {
                prev.left = curr;
            }else{
                prev.right = curr;
            }
        }
    }


}
