package org.satsang.util;

import java.util.HashMap;

import org.satsang.bo.MediaSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.widget.Toast;

/*NOT IN USE*/
public class PlayListUtil {
	static private final Logger Log = LoggerFactory.getLogger(PlayListUtil.class);
	
	
	public void process(Context context, HashMap<String, MediaSchedule> playList) {
		Log.info("Inside process()");
		if(!playList.isEmpty()) {
			/*MediaSchedule bhajan = playList.get(Constants.BHAJAN);
			MediaSchedule pravachan = playList.get(Constants.PRAVACHAN);
			MediaSchedule news = playList.get(Constants.NEWS);
			long bhajanVideoLength = bhajan.getVideoLength();
			long pravachanVideoLength = pravachan.getVideoLength(); //TODO: if pravachan is empty add Mantra audio length
			long newsVideoLength = news.getVideoLength();*/
			
			long bhajanVideoLength=100;
			long pravachanVideoLength=100;
			long newsVideoLength= 15;
			
			int timeDiffInSeconds = ConfigUtil.getScheduleTimeFromConfiguration() * 60;
			if(timeDiffInSeconds <= 0) {
				//check if bhajan and pravachan has elaspsed. If yes then call news timeDiffInSeconds >= (satsangSchedule.getVideoLength())
				timeDiffInSeconds = -timeDiffInSeconds;
				if(timeDiffInSeconds >= (bhajanVideoLength+pravachanVideoLength+newsVideoLength)) {
					//santsang finished
					Toast.makeText(context, "Satsang finished", Toast.LENGTH_LONG).show();
				} else if (timeDiffInSeconds >= (bhajanVideoLength+pravachanVideoLength)){
					//play news
					Toast.makeText(context, "Playing news", Toast.LENGTH_LONG).show();
				} else if (timeDiffInSeconds >= (bhajanVideoLength)) {
					//play pravachan or Mantra
					Toast.makeText(context, "Playing pravachan or mantra", Toast.LENGTH_LONG).show();
				} else {
					//play santsang
					Toast.makeText(context, "Playing bhajan", Toast.LENGTH_LONG).show();
				}
			}else {
				// Play satsang in normal mode
				Toast.makeText(context, "Santsang NORMAL MODE", Toast.LENGTH_LONG).show();
			}	
		}
		
	}
	
}
