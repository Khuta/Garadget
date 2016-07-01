package volpis.com.garadget.interfaces;


import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleDevice;
import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorHolder;

public interface DoorsMVP {


    /**
     * View mandatory methods. Available to Presenter
     * Presenter -> View
     */
    interface RequiredViewOps {
        void setDoors(ArrayList<Door> doors, List<ParticleDevice> devices);

        void showSwipeRefresh(boolean show);

        void showProgress(boolean show);

        void showToast(String message);

        void startAnimation(DoorHolder doorHolder, ImageView imageView, boolean isOpening, long openingTime);

    }

    /**
     * Operations offered from Presenter to View
     * View -> Presenter
     */
    interface PresenterOps {
        void getListOfDevices(ArrayList<DoorHolder> doorHolder);

        void onDestroy();
    }

    /**
     * Model operations offered to Presenter
     * Presenter -> Model
     */
    interface ModelOps {
        void changeDoorStatus(ParticleDevice device, DoorHolder doorHolder, String newStatus);

        void getListOfDevices(ArrayList<DoorHolder> doorHolder);

        void onDestroy();
    }

    /**
     * Operations offered from Presenter to Model
     * Model -> Presenter
     */
    interface RequiredPresenterOps {
        void setDoors(ArrayList<Door> doors, List<ParticleDevice> devices);

        void onSuccess();

        void onFailure(String errorMessage);

        void showSwipeRefresh(boolean show);

        void showProgress(boolean show);

        void showToast(String message);

        void startAnimation(DoorHolder doorHolder, ImageView imageView, boolean isOpening, long openingTime);

    }


}
