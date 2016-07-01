package volpis.com.garadget.interfaces;


import volpis.com.garadget.models.Door;

public interface SettingsMVP {
    /**
     * View mandatory methods. Available to Presenter
     *      Presenter -> View
     */
    interface RequiredViewOps {
        void showToast(String msg);
        void showAlert(String msg);
        void showProgressBar(boolean visible);
        // any other ops
    }

    /**
     * Operations offered from Presenter to View
     *      View -> Presenter
     */
    interface PresenterOps{
        void onDestroy(boolean isChangingConfig);
        void updateConfig(Door door, String config);
        void updateName(String doorId, String name);
        // any other ops to be called from View
    }

    /**
     * Model operations offered to Presenter
     *      Presenter -> Model
     */
    interface ModelOps {
        void updateConfig(String doorId, String config);
        void setDeviceName(String doorId, String name);
        void onDestroy();
        // Any other data operation
    }

    /**
     * Operations offered from Presenter to Model
     *      Model -> Presenter
     */
    interface RequiredPresenterOps {
        void onUpdatesSeved();
        void onError(String errorMsg);
        // Any other returning operation Model -> Presenter
    }


}
