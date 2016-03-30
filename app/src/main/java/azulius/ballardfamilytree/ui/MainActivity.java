package azulius.ballardfamilytree.ui;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import azulius.ballardfamilytree.R;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginSuccessListener {

    private LoginFragment loginFragment;
    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentManager fm = this.getSupportFragmentManager();
        loginFragment = (LoginFragment)fm.findFragmentById(R.id.login_fragment_view);
        if (loginFragment == null) {
            loginFragment = LoginFragment.newInstance();
            fm.beginTransaction()
                    .replace(R.id.current_view, loginFragment)
                    .commit();
        }
    }

    @Override
    public void onLoginSuccess(boolean success) {
        FragmentManager fm = this.getSupportFragmentManager();
         mapFragment = (MapFragment)fm.findFragmentById(R.id.map_fragment_view);
        if (mapFragment == null) {
            mapFragment = mapFragment.newInstance();
            fm.beginTransaction()
                    .replace(R.id.current_view, mapFragment)
                    .commit();
        }
    }
}

