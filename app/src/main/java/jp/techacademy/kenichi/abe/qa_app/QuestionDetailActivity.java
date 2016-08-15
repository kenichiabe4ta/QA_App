package jp.techacademy.kenichi.abe.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class QuestionDetailActivity extends AppCompatActivity{

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private DatabaseReference mAnswerRef;
    private DatabaseReference mQuestionRef;     //Firebase上の質問データの場所(全ジャンル)
    private Button favoriteButton;              //お気に入りボタン追加
    ArrayList<String> favList = new ArrayList<>();//お気に入りkeyリスト

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {        }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {        }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {        }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを収録する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion
                                .getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);


        Log.d("firebase","test");
        favoriteButton = (Button) findViewById(R.id.button);    //お気に入りボタン用リスナ

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            favoriteButton.setVisibility(View.INVISIBLE);       //ログインしていない場合
        }else{
            favoriteButton.setVisibility(View.VISIBLE);         //ログインしている場合
            mQuestionRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre()))
                                                                                  .child(mQuestion.getQuestionUid());

            favList.add("https://qaapp-251e8.firebaseio.com/contents/4/-KP3PVr9HckAjO2G__GU");//ダミーデータ

            // favList<String>の値と質問のkeyが一致したら"お気に入り登録済み"と表示
            mQuestionRef.addListenerForSingleValueEvent(new ValueEventListener() {// user/contents/mGenreのkey取得
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String key = mQuestionRef.getRef().toString();
                    for(String s : favList){
                        if( s.equals( key )){                           //「お気に入り」登録チェック
                            favoriteButton.setText("お気に入り登録済み");
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }
    public void favorite(View v){   //お気に入りボタン
        //お気に入り追加処理
    }

}