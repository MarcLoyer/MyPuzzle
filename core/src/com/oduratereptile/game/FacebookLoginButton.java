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
import de.tomgrill.gdxfacebook.core.GDXFacebookGameRequest;
import de.tomgrill.gdxfacebook.core.GDXFacebookGraphRequest;
import de.tomgrill.gdxfacebook.core.GameRequestResult;
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

    private static final String loginLabel = "Login with Facebook";
    private static final String logoutLabel= "Logout";

    public FacebookLoginButton(GDXFacebook facebook, Skin skin) {
        super(loginLabel, skin);
        this.facebook = facebook;

        permissionsRead.add("email");
        permissionsRead.add("public_profile");
        permissionsRead.add("user_friends");

        addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (loginStatus) {
                    logout();
                } else {
                    loginWithReadPermissions();
                }
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
                sendGameInvitation();
            }

            @Override
            public void onCancel() {
                Gdx.app.debug("debug", "SIGN IN (read permissions): User cancelled login process");
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
        Gdx.app.error("debug", "Logging in");
    }

    private void logout() {
        facebook.signOut();
        Gdx.app.error("debug", "Logged out");
        setPublishButtonStatus(false);
        setLoginButtonStatus(false);
    }

    private void setLoginButtonStatus(boolean loggedIn) {
        loginStatus = loggedIn;
        if (loggedIn) {
            setText(logoutLabel);
        } else {
            setText(loginLabel);
        }
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

                Gdx.app.debug("debug", "Graph Request: successful");
                Gdx.app.error("debug", "  " + fbNickname +", your unique ID is: " + fbIdForThisApp);
                Gdx.app.error("debug", "  json result = " + result.getMessage());
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

    private void sendGameInvitation() {
        GDXFacebookGameRequest request = new GDXFacebookGameRequest();
        request.setMessage("Come play this game!");

        /* TODO Note:
         *  "Game Requests are only available to games."
          *  I apparently have to release the game to the android play store before facebook will
          *  allow me to add this functionality.
          *  Ref: https://developers.facebook.com/docs/games/services/gamerequests
         */
        facebook.gameRequest(request, new GDXFacebookCallback<GameRequestResult>() {
            @Override
            public void onSuccess(GameRequestResult result) {
                Gdx.app.debug("debug", "Game Request: successful");
                Gdx.app.error("debug", "  ID: " + result.getRequestId());
                Gdx.app.error("debug", "  recipients: " + result.getRecipients());
                Gdx.app.error("debug", "  message: " + result.getMessage());
            }

            @Override
            public void onCancel() {
                Gdx.app.debug("debug", "Graph Request: Request cancelled. Reason unknown.");
            }

            @Override
            public void onFail(Throwable t) {
                Gdx.app.error("debug", "Graph Request: Failed with exception.");
                t.printStackTrace();
            }

            @Override
            public void onError(GDXFacebookError error) {
                Gdx.app.error("debug", error.getErrorMessage());
                Gdx.app.error("debug", "Graph Request: Error. Something went wrong with the access token.");
            }
        });
    }
}
