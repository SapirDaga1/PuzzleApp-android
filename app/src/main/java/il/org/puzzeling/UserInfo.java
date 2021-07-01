package il.org.puzzeling;

import java.io.Serializable;

public class UserInfo implements Serializable,Comparable<UserInfo> {
    private String mName;
    private int mScore;
    private String mTime;
    private String mPuzzleImg;
    private boolean mIsAsset;

    public UserInfo(String name, int score, String time, String puzzleImg, boolean isAsset) {
        this.mName = name;
        this.mScore = score;
        this.mTime = time;
        this.mPuzzleImg = puzzleImg;
        this.mIsAsset = isAsset;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public int getScore() {
        return mScore;
    }

    public void setScore(int score) {
        this.mScore = score;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        this.mTime = time;
    }

    public String getPuzzleImg() {
        return mPuzzleImg;
    }

    public void setPuzzleImg(String puzzleImg) {
        this.mPuzzleImg = puzzleImg;
    }

    public boolean isAsset() {
        return mIsAsset;
    }

    public void setIsAsset(boolean isAsset) {
        this.mIsAsset = isAsset;
    }

    public int getTimeValue(){
        int min = Integer.parseInt(this.mTime.split(":")[0]);
        int sec = Integer.parseInt(this.mTime.split(":")[1]);
        return min*60 + sec;
    }

    @Override
    public int compareTo(UserInfo userInfo) {
        if(this.mScore == userInfo.mScore){
            return Integer.compare(this.getTimeValue(),userInfo.getTimeValue());
        } else {
            return Integer.compare(userInfo.mScore, this.mScore);
        }
    }
}
