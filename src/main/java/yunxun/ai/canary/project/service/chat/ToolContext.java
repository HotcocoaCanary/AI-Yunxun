package yunxun.ai.canary.project.service.chat;

import yunxun.ai.canary.project.app.dto.ChatChart;
import yunxun.ai.canary.project.app.dto.ChatSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ToolContext(
        List<ToolTrace> traces,
        List<ChatSource> sources,
        List<ChatChart> charts
) {
    public static ToolContext empty() {
        return new ToolContext(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public ToolContext withTrace(ToolTrace trace) {
        List<ToolTrace> nextTraces = new ArrayList<>(this.traces);
        nextTraces.add(trace);
        return new ToolContext(nextTraces, this.sources, this.charts);
    }

    public ToolContext withSource(ChatSource source) {
        List<ChatSource> nextSources = new ArrayList<>(this.sources);
        nextSources.add(source);
        return new ToolContext(this.traces, nextSources, this.charts);
    }

    public ToolContext withChart(ChatChart chart) {
        List<ChatChart> nextCharts = new ArrayList<>(this.charts);
        nextCharts.add(chart);
        return new ToolContext(this.traces, this.sources, nextCharts);
    }

    @Override
    public List<ToolTrace> traces() {
        return Collections.unmodifiableList(traces);
    }

    @Override
    public List<ChatSource> sources() {
        return Collections.unmodifiableList(sources);
    }

    @Override
    public List<ChatChart> charts() {
        return Collections.unmodifiableList(charts);
    }
}

