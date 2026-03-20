package mahjong;

public enum MeldType {
    KONG(4),
    PONG(3),
    EYES(2),
    LONETILE(1),
    CHOW(0),
    JOINTPARTIAL(-1),
    PARTEDPARTIAL(-2);

    private int num;

    private MeldType(int num){
        this.num = num;
    }

    public int getNum(){ return num; }
}
