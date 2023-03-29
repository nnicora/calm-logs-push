package com.sap.security.poc.schedulers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sap.otel.agentext.api.CsaEventsConverter;
import com.sap.otel.agentext.common.Constants;
import com.sap.security.poc.service.SecurityCollectorService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.events.EventEmitter;
import io.opentelemetry.sdk.logs.SdkEventEmitterProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;


@Service
public class DummyScheduler {
    private Logger LOG = LoggerFactory.getLogger(DummyScheduler.class);

    private final Gson mapper = new Gson();

    private final EventEmitter eventEmitter = SdkEventEmitterProvider
            .create(SdkLoggerProvider.builder().addLogRecordProcessor(new CsaEventsConverter()).build())
            .eventEmitterBuilder("instrumentationScopeName")
            .setEventDomain(CsaEventsConverter.SAP_CALM_CSA.getKey())
            .build();


    @Autowired
    private SecurityCollectorService sec;


    @Scheduled(fixedDelay = 30000)
    public void triggerLogger() {

        String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";

        DateFormat df = new SimpleDateFormat(pattern);
        Date today = Calendar.getInstance().getTime();
        String todayAsString = df.format(today);

        try {
            var o = sec.getRecommendations("ias");
            var str = mapper.toJson(o);
            eventEmitter.emit("IAS_RECOMMENDATIONS", Attributes.of(
                    Constants.ATT_SUBACCOUNT_ID, "37760539-f893-49c8-8ab3-19a0a11116ce",
                    Constants.ATT_CALM_SERVICE_TYPE, "SAP_CP_CF",
                    AttributeKey.stringKey("DataFormat"), "SINGLE_TABLE",
                    AttributeKey.stringKey("ExtractorType"), "SNAPSHOT",
                    AttributeKey.stringKey("SnapshotTimestamp"), todayAsString,
                    AttributeKey.stringKey("StoreData"), Base64.getEncoder().encodeToString(str.getBytes())
                    )
            );
        } catch (Exception e) {
            LOG.error(e.toString());
        }

        try {
            var o = sec.getRecommendations("cf");
            var str = mapper.toJson(o);
            eventEmitter.emit("CF_RECOMMENDATIONS", Attributes.of(
                    Constants.ATT_SUBACCOUNT_ID, "37760539-f893-49c8-8ab3-19a0a11116ce",
                    Constants.ATT_CALM_SERVICE_TYPE, "SAP_CP_CF",
                    AttributeKey.stringKey("DataFormat"), "SINGLE_TABLE",
                    AttributeKey.stringKey("ExtractorType"), "SNAPSHOT",
                    AttributeKey.stringKey("SnapshotTimestamp"), todayAsString,
                    AttributeKey.stringKey("StoreData"), Base64.getEncoder().encodeToString(str.getBytes())
            ));
        } catch (Exception e) {
            LOG.error(e.toString());
        }


    }

}
