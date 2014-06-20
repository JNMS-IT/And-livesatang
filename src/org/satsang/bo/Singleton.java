package org.satsang.bo;
/*
 * This class maintains states of activites that can be access by other parts of application code such
 * as ScheduleReceiver
 */
public class Singleton {
	private static Singleton mInstance = null;
	 
    private boolean isDownloadInProgress=false;
    private boolean isSantsangInProgress=false;
 
    private Singleton(){
;
    }
 
    public static Singleton getInstance(){
        if(mInstance == null)
        {
            mInstance = new Singleton();
        }
        return mInstance;
    }

	public boolean isDownloadInProgress() {
		return this.isDownloadInProgress;
	}

	public void setDownloadInProgress(boolean isDownloadInProgress) {
		this.isDownloadInProgress = isDownloadInProgress;
	}

	public boolean isSantsangInProgress() {
		return this.isSantsangInProgress;
	}

	public void setSantsangInProgress(boolean isSantsangInProgress) {
		this.isSantsangInProgress = isSantsangInProgress;
	}
 
    
}
