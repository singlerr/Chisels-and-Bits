package mod.chiselsandbits.client.logic;

import mod.chiselsandbits.clipboard.CreativeClipboardManager;

public class ClientInitHandler
{

    private ClientInitHandler()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ClientInitHandler. This is a utility class");
    }

    public static void onClientInit() {
        CreativeClipboardManager.getInstance().load();
    }
}
