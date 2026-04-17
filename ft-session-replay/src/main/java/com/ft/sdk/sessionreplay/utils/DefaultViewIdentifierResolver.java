package com.ft.sdk.sessionreplay.utils;

import android.view.View;
import java.security.SecureRandom;

public class DefaultViewIdentifierResolver implements ViewIdentifierResolver {

    private static class SingletonHolder {
        private static final DefaultViewIdentifierResolver INSTANCE = new DefaultViewIdentifierResolver();
    }

    public static DefaultViewIdentifierResolver get() {
        return DefaultViewIdentifierResolver.SingletonHolder.INSTANCE;
    }

    private static final String DATADOG_UNIQUE_IDENTIFIER_KEY_PREFIX = "DATADOG_UNIQUE_IDENTIFIER_";
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public long resolveViewId(View view) {
        // we will use the System.identityHashcode in here which returns a consistent
        // value for an instance even when it is mutable
        return (long) System.identityHashCode(view);
    }

    @Override
    public Long resolveChildUniqueIdentifier(View parent, String childName) {
        int key = (DATADOG_UNIQUE_IDENTIFIER_KEY_PREFIX + childName).hashCode();
        Object uniqueIdentifier = parent.getTag(key);
        if (uniqueIdentifier != null) {
            // the identifier key was registered already by us or because of a collision with
            // client's key. In case was a collision we try our luck and check if the value
            // was a long and use that as an identifier.
            return uniqueIdentifier instanceof Long ? (Long) uniqueIdentifier : null;
        } else {
            long newUniqueIdentifier = (long) secureRandom.nextInt();
            parent.setTag(key, newUniqueIdentifier);
            return newUniqueIdentifier;
        }
    }
}
