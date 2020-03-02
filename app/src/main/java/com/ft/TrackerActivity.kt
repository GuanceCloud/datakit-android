package com.ft

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ft.sdk.FTTrack
import kotlinx.android.synthetic.main.activity_tracker.*
import org.json.JSONObject

class TrackerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        flowChartTacker.setOnClickListener { v ->
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_01", "自定义流程图开始", null, 0, null, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_01", "步骤1", "自定义流程图开始", 1000, null, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_01", "步骤2", "步骤1", 2000, null, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_01", "步骤3", "步骤2", 2000, null, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_01", "选择1", "步骤2", 3000, null, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_01", "步骤4", "选择1", 3000, null, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_01", "步骤4", "步骤3", 3000, null, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_01", "结束", "步骤4", 3000, null, null)
        }

        flowChartTacker1.setOnClickListener { v ->
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_02", "自定义流程图开始", null, 0, JSONObject().apply {
                put("tag","test")
            }, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_02", "步骤1", "自定义流程图开始", 1000, JSONObject().apply {
                put("tag","test")
            }, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_02", "步骤2", "步骤1", 2000, JSONObject().apply {
                put("tag","test")
            }, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_02", "步骤3", "步骤2", 2000, JSONObject().apply {
                put("tag","test")
            }, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_02", "选择1", "步骤2", 3000, JSONObject().apply {
                put("tag","test")
            }, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_02", "步骤4", "选择1", 3000, JSONObject().apply {
                put("tag","test")
            }, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_02", "步骤4", "步骤3", 3000, JSONObject().apply {
                put("tag","test")
            }, null)
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_02", "结束", "步骤4", 3000, JSONObject().apply {
                put("tag","test")
            }, null)
        }

        flowChartTacker2.setOnClickListener { v ->
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_03", "自定义流程图开始", null, 0, null, JSONObject().apply {
                put("\$values","test")
            })
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_03", "步骤1", "自定义流程图开始", 1000, null, JSONObject().apply {
                put("values","test")
            })
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_03", "步骤2", "步骤1", 2000, null, JSONObject().apply {
                put("values","test")
            })
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_03", "步骤3", "步骤2", 2000, null, JSONObject().apply {
                put("values","test")
            })
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_03", "选择1", "步骤2", 3000, null, JSONObject().apply {
                put("values","test")
            })
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_03", "步骤4", "选择1", 3000, null, JSONObject().apply {
                put("values","test")
            })
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_03", "步骤4", "步骤3", 3000, null, JSONObject().apply {
                put("values","test")
            })
            FTTrack.getInstance().trackFlowChart("track_demo", "custom_03", "结束", "步骤4", 3000, null, JSONObject().apply {
                put("values","test")
            })
        }
    }
}
