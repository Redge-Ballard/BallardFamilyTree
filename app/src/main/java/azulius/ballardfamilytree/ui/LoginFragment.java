package azulius.ballardfamilytree.ui;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import azulius.ballardfamilytree.R;
import azulius.ballardfamilytree.model.LoginResult;
import azulius.ballardfamilytree.model.LoginParams;
import azulius.ballardfamilytree.model.Person;

public class LoginFragment extends Fragment {

    private EditText usernameText;
    private EditText passwordText;
    private EditText serverNameText;
    private EditText serverPortText;
    private Button signIn;
    private boolean loginWorked = false;
    private String serverAddress;
    private String serverPort;
    private LoginSuccessListener mCallback;

    public LoginFragment() { }

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Container Activity must implement this interface
    public interface LoginSuccessListener {
        public void onLoginSuccess(boolean success);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity;
        if (context instanceof Activity) {
            activity = (Activity) context;
            try {
                mCallback = (LoginSuccessListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement LoginSuccessListener");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login_view, container, false);

        usernameText = (EditText)v.findViewById(R.id.userField);
        passwordText = (EditText)v.findViewById(R.id.passField);
        serverNameText = (EditText)v.findViewById(R.id.serverField);
        serverPortText = (EditText)v.findViewById(R.id.portField);

        signIn = (Button)v.findViewById(R.id.signInBtn);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginParams params = new LoginParams(usernameText.getText().toString(), passwordText.getText().toString(), serverNameText.getText().toString(), serverPortText.getText().toString());
                signInClicked(params);
            }
        });

        return v;
    }

    private void signInClicked(LoginParams params) {
        LoginResult result = null;
        try {
            SignInTask task = new SignInTask();
            result = task.execute(params).get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (result != null){
            LoadDataTask loadTask = new LoadDataTask();
            loadTask.execute(result);
            LoadAllDataTask loadAllTask = new LoadAllDataTask();
            loadAllTask.execute(result);
        }
    }

    public class SignInTask extends AsyncTask<LoginParams, Integer, LoginResult> {

        @Override
        protected LoginResult doInBackground(LoginParams... params) {
            JSONObject body = new JSONObject();
            try {
                body.put("username", params[0].getUsername());
                body.put("password", params[0].getPassword());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            String postData = body.toString();
            try {
                String address = "http://" + params[0].getServerName() + ":" + params[0].getServerPort() + "/user/login";
                serverAddress = params[0].getServerName();
                serverPort = params[0].getServerPort();
                URL url = new URL(address);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.connect();
                OutputStream requestBody = connection.getOutputStream();
                requestBody.write(postData.getBytes());
                requestBody.close();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream responseBody = connection.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    while ((length = responseBody.read(buffer)) != -1) {
                        baos.write(buffer, 0, length);
                    }
                    String responseBodyData = baos.toString();
                    JSONObject response = new JSONObject(responseBodyData);
                    LoginResult result = new LoginResult(response.getString("Authorization"),response.getString("userName"),
                            response.getString("personId"));
                    return result;
                }
            } catch (Exception e) {
                String error = e.getMessage();
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(LoginResult result) {
            String outcome = new String();
            if (result != null){
                outcome = "Login successful.";
                loginWorked = true;
                //mCallback.onLoginSuccess(loginWorked);
            }
            else {
                outcome = "Login failed.";
            }
            Toast.makeText(getActivity().getBaseContext(), outcome, Toast.LENGTH_LONG).show();
        }
    }

    public class LoadDataTask extends AsyncTask<LoginResult, Integer, Person> {

        protected Person doInBackground(LoginResult... params) {
            try {
                String address = "http://" + serverAddress + ":" + serverPort + "/person/" + params[0].getPersonId();
                URL url = new URL(address);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", params[0].getAuthorization());
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream responseBody = connection.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    while ((length = responseBody.read(buffer)) != -1) {
                        baos.write(buffer, 0, length);
                    }
                    String responseBodyData = baos.toString();
                    JSONObject response = new JSONObject(responseBodyData);
                    Person result = getPersonFromJson(response);
                    return result;
                }
            } catch (Exception e) {
                String error = e.getMessage();
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Person result) {
            String outcome = new String();
            if (result != null){
                outcome = result.getFirstName() + " " + result.getLastName() + " successfully loaded.";
                loginWorked = true;
                mCallback.onLoginSuccess(loginWorked);
            }
            else {
                outcome = "Load failed.";
            }
            Toast.makeText(getActivity(), outcome, Toast.LENGTH_LONG).show();
        }
    }

    public class LoadAllDataTask extends AsyncTask<LoginResult, Integer, ArrayList<Person>> {

        protected ArrayList<Person> doInBackground(LoginResult... params) {
            try {
                String address = "http://" + serverAddress + ":" + serverPort + "/person/";
                URL url = new URL(address);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", params[0].getAuthorization());
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream responseBody = connection.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length = 0;
                    while ((length = responseBody.read(buffer)) != -1) {
                        baos.write(buffer, 0, length);
                    }
                    String responseBodyData = baos.toString();
                    JSONObject response = new JSONObject(responseBodyData);
                    String dataArray = response.getString("data");
                    JSONArray responseArray = new JSONArray(dataArray);
                    ArrayList<Person> peopleList = new ArrayList<>();
                    for (int i = 0; i < responseArray.length(); i++){
                        Person person = getPersonFromJson(responseArray.getJSONObject(i));
                        peopleList.add(person);
                    }
                    return peopleList;
                }
            } catch (Exception e) {
                String error = e.getMessage();
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(ArrayList<Person> result) {
            String outcome = new String();
            if (result != null){
                loginWorked = true;
                mCallback.onLoginSuccess(loginWorked);
            }
            else {
                outcome = "Load failed.";
            }
            Toast.makeText(getActivity(), outcome, Toast.LENGTH_LONG).show();
        }
    }

    public Person getPersonFromJson(JSONObject obj){
        String first;
        String last;
        String gender;
        String id;
        String descendant;
        String father;
        String mother;
        String spouse;
        try {
            first = obj.getString("firstName");
            last = obj.getString("lastName");
            gender = obj.getString("gender");
            id = obj.getString("personID");
            descendant = obj.getString("descendant");
            if (obj.has("father")) {
                father = obj.getString("father");
            }
            else {
                father = null;
            }
            if (obj.has("mother")) {
                mother = obj.getString("mother");
            }
            else {
                mother = null;
            }
            if (obj.has("spouse")) {
                spouse = obj.getString("spouse");
            }
            else {
                spouse = null;
            }
            Person person = new Person(first, last, gender, id, descendant, father, mother, spouse);
            return person;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
