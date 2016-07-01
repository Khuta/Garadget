package volpis.com.garadget.interfaces;


import com.google.android.gms.maps.model.LatLng;

import volpis.com.garadget.models.Door;
import volpis.com.garadget.models.DoorLocation;

public interface AlertsMVP {
    /**
     * View mandatory methods. Available to Presenter
     * Presenter -> View
     */
    interface RequiredViewOps {
        void showToast(String msg);

        void showAlert(String msg);

        void showProgressBar(boolean visible);

        void moveMap(LatLng latLng);

        String getAction(boolean toggleStatus);

        void fillData(Door door, DoorLocation doorLocation);

        void fillRadiusText(int radius);

        void showMarkers(String doorName, LatLng doorLatLng, double radius);
    }

    /**
     * Operations offered from Presenter to View
     * View -> Presenter
     */
    interface PresenterOps {
        void onDestroy(boolean isChangingConfig);

        void moveMap(LatLng latLng);

        void notifyBackend(boolean toggleStatus);

        void setLocationChangedListener();

        void writeMapData(LatLng latLng);

        void removeDoorLocation();

        void setDoor(Door door);

        void updateConfig(String newConfig);

        void onMapReady();

        void radiusSelected(int radius);

        void flipBit(int position);

        void onMapLongClick(LatLng latLng);

        void setSelectedRadius(int radius);
    }

    /**
     * Model operations offered to Presenter
     * Presenter -> Model
     */
    interface ModelOps {
        void notifyBackend(String action);

        void setLocationChangedListener();

        void writeMapData(LatLng latLng);

        void removeDoorLocation();

        void setDoor(Door door);

        void updateConfig(String newConfig);

        void onMapReady();

        void radiusSelected(int radius);

        void flipBit(int position);

        void onMapLongClick(LatLng latLng);

        void setSelectedRadius(int radius);
    }

    /**
     * Operations offered from Presenter to Model
     * Model -> Presenter
     */
    interface RequiredPresenterOps {
        void showToast(String msg);

        void onUpdatesSaved(String message);

        void onError(String errorMsg);

        void moveMap(LatLng latLng);

        void fillData(Door door, DoorLocation doorLocation);

        void fillRadiusText(int radius);

        void showMarkers(String doorName, LatLng doorLatLng, double radius);
    }


}
