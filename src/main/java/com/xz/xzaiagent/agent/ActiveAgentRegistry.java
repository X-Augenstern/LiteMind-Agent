package com.xz.xzaiagent.agent;

import com.xz.xzaiagent.agent.model.AgentState;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry to track active agents / SSE emitters / reactive disposables by chatId/requestId.
 */
@Component
public class ActiveAgentRegistry {

    public static class Entry {
        public BaseAgent agent;
        public SseEmitter emitter;
        public Disposable disposable;

        public Entry(BaseAgent agent, SseEmitter emitter, Disposable disposable) {
            this.agent = agent;
            this.emitter = emitter;
            this.disposable = disposable;
        }
    }

    private final Map<String, Entry> map = new ConcurrentHashMap<>();

    public void register(String id, BaseAgent agent, SseEmitter emitter, Disposable disposable) {
        if (id == null) return;
        map.put(id, new Entry(agent, emitter, disposable));
    }

    public void unregister(String id) {
        if (id == null) return;
        map.remove(id);
    }

    /**
     * Terminate an active entry.
     *
     * @param id   chatId
     * @param hard if true, also set agent state to FINISHED; if false, only dispose/complete local handles
     * @return whether an entry was found and handled
     */
    public boolean terminate(String id, boolean hard) {
        if (id == null) return false;
        Entry entry = map.get(id);
        if (entry == null) return false;

        // dispose reactive subscription (stop current streaming work)
        if (entry.disposable != null) {
            try {
                entry.disposable.dispose();
            } catch (Exception ignored) {
            } finally {
                entry.disposable = null;
            }
        }

        // complete SSE emitter (notify client)
        if (entry.emitter != null) {
            try {
                entry.emitter.send("任务已被用户终止。");
            } catch (IOException ignored) {
            } finally {
                try {
                    entry.emitter.complete();
                } catch (Exception ignored) {
                }
                // clear local emitter reference for soft-terminate
                entry.emitter = null;
            }
        }

        if (hard) {
            // remove registry entry and mark agent finished
            map.remove(id);
            if (entry.agent != null) {
                try {
                    // set finished state and clear runtime messageList to fully terminate
                    entry.agent.setState(AgentState.FINISHED);
                    try {
                        entry.agent.getMessageList().clear();
                    } catch (Exception ignored) {
                    }
                } catch (Exception ignored) {
                }
            }

            // soft terminate: keep registry entry (agent context/memory) but clear local handles
            // agent remains in registry for future reuse; not setting FINISHED
        }

        return true;
    }
}


