package com.example.alex.gravityball;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    float xPos, yPos, xAcc, yAcc, xVel, yVel = 0.0f; // position, acceleration ,velocity
    float screenWidth, screenHeight;
    Sensor accel;
    int ballRadius = 100;
    Bitmap ball;
    GameView ballView;
    SensorManager sMgr;
    int score;
    Chronometer mChronometer;
    long timeBeforePause;
    boolean gameover;
    double finalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ballView = new GameView(this);
        setContentView(ballView);

        //Getting screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels - ballRadius;
        screenHeight = displayMetrics.heightPixels - ballRadius;

        //managing sensors
        sMgr = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sMgr.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);

        //initialising game
        score = 99;
        gameover = false;

        //chronometer
        mChronometer = new Chronometer(this);
        timeBeforePause = 0;
    }


    @Override
    protected void onPause() {
        sMgr.unregisterListener(this);
        super.onPause();
        timeBeforePause = mChronometer.getBase() - SystemClock.elapsedRealtime();
        mChronometer.stop();
    }
    protected void onResume() {
        super.onResume();
        sMgr.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
        mChronometer.setBase(SystemClock.elapsedRealtime() + timeBeforePause);
        mChronometer.start();
    }
    protected void onStart()
    {
        super.onStart();
        mChronometer.start();
    }
    @Override
    public void onSensorChanged(SensorEvent ev) {
        if (ev.sensor == accel) {
            xAcc = ev.values[0];
            yAcc = -ev.values[1];
            updateBall();
        }
    }

    private void updateBall() {
        float deltaTime = 40f; // fps
        xVel += (xAcc / deltaTime);// v = acc / time
        yVel += (yAcc / deltaTime);

        xPos -= xVel * deltaTime; // x2 = x1 - d WHERE d = vel * time
        yPos -= yVel * deltaTime;

        //screen border
        if (xPos > screenWidth) {
            xPos = screenWidth;
            xVel = -xVel/2; //bounciness
        } else if (xPos < 0) {
            xPos = 0;
            xVel = -xVel/2; //bounciness
        }
        if (yPos > screenHeight) {
            yPos = screenHeight;
            yVel = -yVel/2; //bounciness
        } else if (yPos < 0) {
            yPos = 0;
            yVel = -yVel/2; //bounciness
        }

        //collision with goal
        if(ballView.reached((int)xPos,(int)yPos)) {
            ballView.update();
            score += 1;
        }
        if(score == 100) {
            if (!gameover) {
                mChronometer.stop();
                finalTime = (double)(SystemClock.elapsedRealtime() - mChronometer.getBase())/1000;
                Toast toast = Toast.makeText(this, "GAME OVER! Your time was: " + finalTime + " seconds", Toast.LENGTH_LONG);
                toast.show();
                gameover = true;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class GameView extends View {
        Rect rectangle;
        Paint paint;

        public GameView(Context context) {
            super(context);
            //ball
            Bitmap ballSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
            ball = Bitmap.createScaledBitmap(ballSrc, ballRadius, ballRadius, true);

            //set-up initial goal
            rectangle = new Rect(500,750,600,850);

            //set-up paint
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(3);
            paint.setTextSize(75);
        }
        @Override
        public void onDraw(Canvas canvas) {
            if(!gameover) {
                //ball
                canvas.drawBitmap(ball, xPos, yPos, null);
                //goal
                paint.setColor(Color.RED);
                canvas.drawRect(rectangle, paint);
                //scoreboard
                paint.setColor(Color.GREEN);
                canvas.drawText("SCORE: " + score, 10, 60, paint);
                //redraw canvas
                invalidate();
            }else{
                paint.setColor(Color.GREEN);
                paint.setTextSize(75);
                paint.setStrokeWidth(5);
                canvas.drawText("FINAL TIME: " + finalTime, 30, screenHeight/2, paint);
            }
        }
        public void update() {
            //generate random position on screen
            int left = (int)(Math.random()*(int)screenWidth);
            int top = (int)(Math.random()*(int)screenHeight);
            int right = left + 100 - score + 1;// increase difficulty
            int bottom = top + 100 - score + 1;
            rectangle = new Rect(left,top,right,bottom);
        }
        public boolean reached(int x,int y) {
            //check if ball's position is within the rectangle's perimeter
            return (x + ballRadius) >= this.rectangle.left && x <= this.rectangle.right && (y + ballRadius) >= this.rectangle.top && y <= this.rectangle.bottom;
        }

    }
}
