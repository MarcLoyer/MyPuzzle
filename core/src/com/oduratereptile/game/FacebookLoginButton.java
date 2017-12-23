package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;

import de.tomgrill.gdxfacebook.core.GDXFacebook;
import de.tomgrill.gdxfacebook.core.GDXFacebookCallback;
import de.tomgrill.gdxfacebook.core.GDXFacebookError;
import de.tomgrill.gdxfacebook.core.GDXFacebookGraphRequest;
import de.tomgrill.gdxfacebook.core.JsonResult;
import de.tomgrill.gdxfacebook.core.SignInMode;
import de.tomgrill.gdxfacebook.core.SignInResult;

/**
 * Created by Marc on 12/22/2017.
 */

public class FacebookLoginButton extends TextButton {
    GDXFacebook facebook;
    private Array<String> permissionsRead = new Array<String>();
    private boolean loginStatus = false;
    private boolean publishStatus = false;

    public FacebookLoginButton(GDXFacebook facebook, Skin skin) {
        this(facebook, "Login with Facebook", skin);
    }

    public FacebookLoginButton(GDXFacebook facebook, String label, Skin skin) {
        super(label, skin);
        this.facebook = facebook;

        permissionsRead.add("email");
        permissionsRead.add("public_profile");
        permissionsRead.add("user_friends");

        addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                loginWithReadPermissions();
            }
        });
    }

    public void loginWithReadPermissions() {
        facebook.signIn(SignInMode.READ, permissionsRead, new GDXFacebookCallback<SignInResult>() {

            @Override
            public void onSuccess(SignInResult result) {
                Gdx.app.debug("debug", "SIGN IN (read permissions): User signed in successfully.");

                gainMoreUserInfo();
                setPublishButtonStatus(true);
                setLoginButtonStatus(true);
            }

            @Override
            public void onCancel() {
                Gdx.app.debug("debug", "SIGN IN (read permissions): User canceled login process");
            }

            @Override
            public void onFail(Throwable t) {
                Gdx.app.error("debug", "SIGN IN (read permissions): Technical error occured:");
                logout();
                t.printStackTrace();
            }

            @Override
            public void onError(GDXFacebookError error) {
                Gdx.app.error("debug", "SIGN IN (read permissions): Error login: " + error.getErrorMessage());
                logout();
            }

        });
    }

    private void logout() {
        facebook.signOut();
        Gdx.app.error("debug", "Logged out");
        setPublishButtonStatus(false);
        setLoginButtonStatus(false);
    }

    private void setLoginButtonStatus(boolean loggedIn) {
        loginStatus = loggedIn;
        setDisabled(loggedIn);
    }

    private void setPublishButtonStatus(boolean enabled) {
        publishStatus = enabled;
    }

    private void gainMoreUserInfo() {
        GDXFacebookGraphRequest request = new GDXFacebookGraphRequest().setNode("me").useCurrentAccessToken();

        facebook.graph(request, new GDXFacebookCallback<JsonResult>() {
            @Override
            public void onSuccess(JsonResult result) {
                JsonValue root = result.getJsonValue();

                String fbNickname = root.getString("name");
                String fbIdForThisApp = root.getString("id");

                Gdx.app.debug("debug", "Graph Reqest: successful");
                Gdx.app.error("debug", "  " + fbNickname +", your unique ID is: " + fbIdForThisApp);
            }

            @Override
            public void onCancel() {
                logout();
                Gdx.app.debug("debug", "Graph Reqest: Request cancelled. Reason unknown.");
            }

            @Override
            public void onFail(Throwable t) {
                Gdx.app.error("debug", "Graph Reqest: Failed with exception.");
                logout();
                t.printStackTrace();
            }

            @Override
            public void onError(GDXFacebookError error) {
                Gdx.app.error("debug", error.getErrorMessage());
                Gdx.app.error("debug", "Graph Reqest: Error. Something went wrong with the access token.");
                logout();
            }
        });
    }
}
