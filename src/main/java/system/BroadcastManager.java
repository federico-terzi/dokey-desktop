package system;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Singleton used to propagate events to the entire application
 * and to decouple elements.
 */
public class BroadcastManager {
    // Events
    public static final int EDITOR_MODIFIED_SECTION_EVENT = 10;

    public static final int OPEN_CONTROL_PANEL_REQUEST_EVENT = 20;
    public static final int OPEN_SETTINGS_REQUEST_EVENT = 21;
    public static final int OPEN_COMMANDS_REQUEST_EVENT = 22;

    public static final int ADD_URL_TO_QUICK_COMMANDS_EVENT = 30;

    public static final int ENABLE_DOKEY_SEARCH_PROPERTY_CHANGED = 100;


    // internal variables
    private static BroadcastManager instance = null;
    private Thread eventThread = null;

    // Event queue
    private BlockingQueue<QueueEntry> eventQueue = new LinkedBlockingQueue<>();

    /**
     * Class used as an entry for the event queue
     */
    class QueueEntry {
        int eventID;
        Serializable param;
    }

    // Observer map
    private HashMap<Integer, ArrayList<BroadcastListener>> eventObservers = new HashMap<Integer, ArrayList<BroadcastListener>>();

    public interface BroadcastListener {
        void onBroadcastReceived(Serializable param);
    }

    // Create the logger
    private final static Logger LOG = Logger.getGlobal();

    private BroadcastManager() {
        super();
        // Create the event thread and start it
        eventThread = new Thread(eventLoop);
        eventThread.setName("Broadcast Manager Event Loop");
        eventThread.start();
    }

    /**
     * Get the instance of the BroadcastManager.
     * The event loop will be started if not already running.
     * @return the BroadcastManager instance.
     */
    public static BroadcastManager getInstance() {
        if (instance == null) {
            instance = new BroadcastManager();
        }
        return instance;
    }

    /**
     * The event loop that will serve all the broadcast requests.
     */
    private Runnable eventLoop = new Runnable() {
        @Override
        public void run() {
            // Dispatch all the events
            try {
                while (true) {
                    QueueEntry entry = eventQueue.take();

                    // Make sure the event type is valid
                    if (eventObservers.containsKey(entry.eventID)) {
                        // Dispatch the event to all the observers
                        ArrayList<BroadcastListener> observers = eventObservers.get(entry.eventID);
                        for (BroadcastListener listener : observers) {
                            try {
                                listener.onBroadcastReceived(entry.param);
                            }catch(Exception e) {
                                LOG.warning(e.toString());
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Register a BroadcastListener for a particular event.
     * @param eventID the type of event.
     * @param listener the BroadcastListener object.
     */
    public void registerBroadcastListener(int eventID, BroadcastListener listener) {
        // If not present, create the list for the specified event id
        if (!eventObservers.containsKey(eventID)) {
            eventObservers.put(eventID, new ArrayList<BroadcastListener>());
        }

        // Add the listener if not already present
        if (!eventObservers.get(eventID).contains(listener)) {
            eventObservers.get(eventID).add(listener);
        }
    }

    /**
     * Unregister a BroadcastListener from a particular event.
     * @param eventID the type of event.
     * @param listener the BroadcastListener object.
     */
    public void unregisterBroadcastListener(int eventID, BroadcastListener listener) {
        // Make sure the event id exists in the map
        if (eventObservers.containsKey(eventID)) {
            eventObservers.get(eventID).remove(listener);
        }
    }

    /**
     * Send a Broadcast to all the BroadcastListeners previously registered
     * for that particular eventID
     * @param eventID the type of event to broadcast
     * @param param an optional parameter to pass. ( It will be copied ).
     * @return true if succeeded, false otherwise.
     */
    public boolean sendBroadcast(int eventID, Serializable param) {
        Serializable copyParam = null;

        // Copy the param object to improve decoupling if specified
        if (param != null) {
            try {
                copyParam = deepCopy(param);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        // Create the entry
        QueueEntry entry = new QueueEntry();
        entry.eventID = eventID;
        entry.param = copyParam;

        // Add to the queue
        eventQueue.add(entry);

        LOG.fine("BROADCAST: "+ eventID);

        return true;
    }

    /**
     * Used to copy a serializable object
     * @param oldObj the object to copy.
     * @return the copy of the original object
     * @throws Exception
     */
    private static Serializable deepCopy(Serializable oldObj) throws Exception
    {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try
        {
            ByteArrayOutputStream bos =
                    new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            // serialize and pass the object
            oos.writeObject(oldObj);
            oos.flush();
            ByteArrayInputStream bin =
                    new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bin);
            // return the new object
            return (Serializable) ois.readObject();
        }
        catch(Exception e)
        {
            LOG.warning(e.toString());
            throw(e);
        }
        finally
        {
            oos.close();
            ois.close();
        }
    }
}
