package com.ipvans.mailtest.tile;

public class VelocityDecelerator {
  public static final float FRICTIONAL_DECELERATION = 0.01f;
  private int mDirectionX = 1;
  private int mDirectionY = 1;
  private float mVelocityX = 0;
  private float mVelocityY = 0;
  private float mPreviousVelocityX = 0;
  private float mPreviousVelocityY = 0;
  private long mStartTimeX = 0;
  private long mStartTimeY = 0;
  private long mPreviousTimeX = 0;
  private long mPreviousTimeY = 0;
  private long mCurrentTimeX = 0;
  private long mCurrentTimeY = 0;
  private float mTotalTimeX = 0;
  private float mTotalTimeY = 0;
  private long mCurrentTime;
  private float mDeltaTimeX;
  private float mDeltaTimeY;
  private float mTempDistance;

  public VelocityDecelerator(float velocityX, float velocityY) {
    start(velocityX, velocityY);
  }

  public void stop() {
    mVelocityX = mPreviousVelocityX = 0;
    mVelocityY = mPreviousVelocityY = 0;
    mTotalTimeX = 0;
    mTotalTimeY = 0;
    mStartTimeX = 0;
    mStartTimeY = 0;
  }

  public void start(float velocityX, float velocityY) {
    mDirectionX = (velocityX > 0 ? 1 : -1);
    mDirectionY = (velocityY > 0 ? 1 : -1);
    mVelocityX = mPreviousVelocityX = Math.abs(velocityX);
    mVelocityY = mPreviousVelocityY = Math.abs(velocityY);
    mCurrentTime = System.currentTimeMillis();
    mStartTimeX = mCurrentTime;
    mCurrentTimeX = mCurrentTime;
    mPreviousTimeX = mCurrentTime;
    mStartTimeY = mCurrentTime;
    mCurrentTimeY = mCurrentTime;
    mPreviousTimeY = mCurrentTime;
    mTotalTimeX = Math.abs((velocityX / FRICTIONAL_DECELERATION));
    mTotalTimeY = Math.abs((velocityY / FRICTIONAL_DECELERATION));
  }

  public int getDirectionX() {
    return mDirectionX;
  }

  public int getDirectionY() {
    return mDirectionY;
  }

  public boolean isMoving() {
    return (getSpeedX() > 0 || getSpeedY() > 0);
  }

  private void updateTimeX() {
    mCurrentTimeX = System.currentTimeMillis();
  }

  private void updateTimeY() {
    mCurrentTimeY = System.currentTimeMillis();
  }

  public void calculateFreezeFrameData() {
    mPreviousTimeX = mCurrentTimeX;
    mPreviousTimeY = mCurrentTimeY;
    mPreviousVelocityX = getSpeedX();
    mPreviousVelocityY = getSpeedY();
    updateTimeX();
    updateTimeY();
  }

  private float getSpeedX() {
    mDeltaTimeX = (mCurrentTimeX - mStartTimeX);
    if (mDeltaTimeX >= mTotalTimeX) return 0;
    return mVelocityX - FRICTIONAL_DECELERATION * mDeltaTimeX;
  }

  private float getSpeedY() {
    mDeltaTimeY = (mCurrentTimeY - mStartTimeY);
    if (mDeltaTimeY >= mTotalTimeY) return 0;
    return mVelocityY - FRICTIONAL_DECELERATION * mDeltaTimeY;
  }

  public float getDeltaDistanceX() {
    mDeltaTimeX = (mCurrentTimeX - mPreviousTimeX);
    mTempDistance = mPreviousVelocityX * mDeltaTimeX
        - (FRICTIONAL_DECELERATION * mDeltaTimeX * mDeltaTimeX) / 2;
    return (mTempDistance < 0 ? 0 : mTempDistance);
  }

  public float getDeltaDistanceY() {
    mDeltaTimeY = (mCurrentTimeY - mPreviousTimeY);
    mTempDistance = mPreviousVelocityY * mDeltaTimeY
        - (FRICTIONAL_DECELERATION * mDeltaTimeY * mDeltaTimeY) / 2;
    return (mTempDistance < 0 ? 0 : mTempDistance);
  }

  public float getCurrentDistanceX() {
    mDeltaTimeX = (mCurrentTimeX - mStartTimeX);
    mTempDistance =
        mVelocityX * mDeltaTimeX - (FRICTIONAL_DECELERATION * mDeltaTimeX * mDeltaTimeX) / 2;
    return (mTempDistance < 0 ? 0 : mTempDistance);
  }

  public float getCurrentDistanceY() {
    mDeltaTimeY = (mCurrentTimeY - mStartTimeY);
    mTempDistance =
        mVelocityY * mDeltaTimeY - (FRICTIONAL_DECELERATION * mDeltaTimeY * mDeltaTimeY) / 2;
    return (mTempDistance < 0 ? 0 : mTempDistance);
  }

  public float getTotalDistanceX() {
    return mVelocityX * mTotalTimeX - (FRICTIONAL_DECELERATION * mTotalTimeX * mTotalTimeX) / 2;
  }

  public float getTotalDistanceY() {
    return mVelocityY * mTotalTimeY - (FRICTIONAL_DECELERATION * mTotalTimeY * mTotalTimeY) / 2;
  }
}