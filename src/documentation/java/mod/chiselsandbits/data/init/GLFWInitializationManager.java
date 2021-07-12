package mod.chiselsandbits.data.init;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLFWInitializationManager
{
    private static final GLFWInitializationManager INSTANCE = new GLFWInitializationManager();

    public static GLFWInitializationManager getInstance()
    {
        return INSTANCE;
    }

    private boolean initialized = false;

    private final GLFWErrorCallback loggingErrorCallback = GLFWErrorCallback.create((error, description) -> {
        System.err.println("Error "+error+": "+description);
    });

    private GLFWInitializationManager()
    {
    }

    void initialize() {
        if (initialized)
            return;

        initialized = true;
        // Hack together something that may work?
        if(!glfwInit())
            throw new RuntimeException("Failed to initialize GLFW???");
        glfwSetErrorCallback(loggingErrorCallback);
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        long window = glfwCreateWindow(512, 512, "Hello World!", NULL, NULL);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
    }
}
