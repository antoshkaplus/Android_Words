package com.antoshkaplus.words;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

/**
 * Created by Anton.Logunov on 4/18/2015.
 */
public class CredentialFactory {

    static GoogleAccountCredential create(Context context, String accountName) {
        final String WEB_CLIENT_ID =
                "server:client_id:251166830439-2noub1jvf90q79oc87sgbho3up8iurej.apps.googleusercontent.com";

        GoogleAccountCredential credential = GoogleAccountCredential.usingAudience(
                context, WEB_CLIENT_ID);
        credential.setSelectedAccountName(accountName);
        return credential;
    }



}