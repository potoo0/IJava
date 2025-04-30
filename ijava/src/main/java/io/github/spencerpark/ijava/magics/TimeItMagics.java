package io.github.spencerpark.ijava.magics;

import io.github.spencerpark.ijava.IJava;
import io.github.spencerpark.jupyter.kernel.magic.registry.CellMagic;

import java.util.*;
import java.util.stream.Collectors;

public class TimeItMagics {
    private static final int EPOCH_DEFAULT = 3;
    private static final int LOOP_DEFAULT = 5;

    @CellMagic(aliases = {"time", "timeit"})
    public void timeIt(List<String> args, String body) throws Exception {
        if (args == null) args = Collections.emptyList();

        if (!args.isEmpty() && ("-h".equals(args.get(0)) || "--help".equals(args.get(0)))) {
            System.out.println("help: \nexample: \n");
            System.out.println("%%time epochs=3 loops=5\n1 + 1");
            return;
        }

        // parse input args
        Map<String, Integer> params = args.stream()
                .map(arg -> arg.split("="))
                .filter(kv -> kv.length > 0 && isNotEmpty(kv[0]) && isNotEmpty(kv[1]) && kv[1].matches("\\d+"))
                .collect(Collectors.toMap(kv -> kv[0], kv -> Integer.parseInt(kv[1])));

        // for each epoch
        Integer epochNum = params.getOrDefault("epochs", EPOCH_DEFAULT);
        Integer loopNum = params.getOrDefault("loops", LOOP_DEFAULT);
        List<List<Long>> epochData = new ArrayList<>(epochNum);
        for (int i = 0; i < epochNum; i++) {
            // for each loop
            List<Long> loopData = new ArrayList<>(loopNum);
            for (int j = 0; j < loopNum; j++) {
                loopData.add(System.currentTimeMillis());
                IJava.getKernelInstance().evalRaw(body);
                loopData.add(System.currentTimeMillis());
            }
            epochData.add(loopData);
        }

        // Summary Statistics
        List<List<Long>> epochDiff = new ArrayList<>(epochData.size());
        for (int i = 0; i < epochData.size(); i++) {
            List<Long> loopData = epochData.get(i);
            List<Long> diff = new ArrayList<>(loopData.size() / 2);
            for (int j = 0; j < loopData.size() / 2; j++) {
                diff.add(loopData.get(i * 2 + 1) - loopData.get(i * 2));
            }
            LongSummaryStatistics statistics = diff.stream().collect(Collectors.summarizingLong(o -> o));
            System.out.printf("epoch %d: %s%n", i, statistics);
            epochDiff.add(diff);
        }
        System.out.printf("total: %s%n", epochDiff.stream().flatMap(Collection::stream).collect(Collectors.summarizingLong(o -> o)));
    }

    static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}
