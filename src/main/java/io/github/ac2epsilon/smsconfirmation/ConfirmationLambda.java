package io.github.ac2epsilon.smsconfirmation;

/**
 * Created by ac2 on 01.02.17.
 */

/**
 * Supporting interface to make functional calls in iterate()
 */
interface ConfirmationLambda {
    void run(Confirmation confirmation);
}
