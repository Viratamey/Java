package concurrencyMultithreading.thread;

public class IntWrapperKey {

    private Object key = new Object();
    IntWrapperKey(int i) {
        intWrapper = i;
    }
    private int intWrapper;

    public int getIntWrapper() {
        synchronized(key) {
            return intWrapper;
        }
    }

    public void setIntWrapper(int intWrapper) {
        synchronized(key) {
            this.intWrapper = intWrapper;
        }
    }

}
