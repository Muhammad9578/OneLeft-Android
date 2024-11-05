package com.oneleft.app.utils;

public class ApiConfig {

    private static final String BASE_URL = "https://uppi.androidworkshop.net/";

    public static final String SEND_OTP_URL = BASE_URL + "send_otp.php";

    public static final String VERIFY_OTP_URL = BASE_URL + "verify_otp.php";

    //public static final String FORGOT_PASSWORD_URL = BASE_URL + "forgot_password.php";

    public static final String PAYMENT_INTENT_URL = BASE_URL + "stripe_api.php?action=create_payment_intent";

    public static final String CREATE_STRIPE_ACCOUNT_URL = BASE_URL + "stripe_api.php?action=create_account";

    public static final String GET_STRIPE_ACCOUNT_DETAILS_URL = BASE_URL + "stripe_api.php?action=get_account_detail";

    public static final String CLAIM_REWARD_URL = BASE_URL + "stripe_api.php?action=claim_reward";


}
