package mod.chiselsandbits.notifications;

import mod.chiselsandbits.api.notifications.INotification;
import mod.chiselsandbits.api.notifications.INotificationManager;
import mod.chiselsandbits.client.screens.components.toasts.ChiselsAndBitsNotificationToast;
import mod.chiselsandbits.platforms.core.dist.Dist;
import mod.chiselsandbits.platforms.core.dist.DistExecutor;

public final class NotificationManager implements INotificationManager
{
    private static final NotificationManager INSTANCE = new NotificationManager();

    public static NotificationManager getInstance()
    {
        return INSTANCE;
    }

    private NotificationManager()
    {
    }

    @Override
    public void notify(final INotification notification)
    {
        DistExecutor.unsafeRunWhenOn(
          Dist.CLIENT,
          () -> () -> ChiselsAndBitsNotificationToast.notifyOf(notification)
        );
    }
}
