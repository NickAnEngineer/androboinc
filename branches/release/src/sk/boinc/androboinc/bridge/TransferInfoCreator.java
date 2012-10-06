/* 
 * AndroBOINC - BOINC Manager for Android
 * Copyright (C) 2010, Pavol Michalec
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package sk.boinc.androboinc.bridge;

import sk.boinc.androboinc.R;
import sk.boinc.androboinc.clientconnection.TransferInfo;
import android.content.res.Resources;
import edu.berkeley.boinc.lite.Transfer;


public class TransferInfoCreator {
	public static final int ERR_GIVEUP_DOWNLOAD = -114;
	public static final int ERR_GIVEUP_UPLOAD = -115;

	public static TransferInfo create(final Transfer transfer, final String projectName, final Formatter formatter) {
		Resources resources = formatter.getResources();
		StringBuilder sb = formatter.getStringBuilder();
		int stateControl = transfer.status;
		float pctDone = (float)transfer.bytes_xferred / transfer.nbytes * 100;
		if (pctDone > 100) pctDone = 100.0F;
		int progInd = (int)pctDone;
		String progress = String.format("%.3f%%", pctDone);
		sb.append(formatter.formatSize(transfer.bytes_xferred));
		sb.append(" / ");
		sb.append(formatter.formatSize(transfer.nbytes));
		String size = sb.toString();
		String elapsed = Formatter.formatElapsedTime(transfer.time_so_far);
		String speed = formatter.formatSpeed(transfer.xfer_speed);
		stateControl = 0;
		sb.setLength(0);
		if (transfer.time_so_far > 0) {
			// This transfer already started
			stateControl |= TransferInfo.STARTED;
		}
		if (transfer.next_request_time > (System.currentTimeMillis() / 1000)) {
			// Suspended for some time
			long toGo = (transfer.next_request_time - System.currentTimeMillis() / 1000);
			sb.append(resources.getString(R.string.retryIn));
			sb.append(" ");
			sb.append(Formatter.formatElapsedTime(toGo));
			stateControl |= TransferInfo.SUSPENDED;
		}
		else if (transfer.status == ERR_GIVEUP_DOWNLOAD) {
			sb.append(resources.getString(R.string.downloadFailed));
			stateControl |= TransferInfo.FAILED;
		}
		else if (transfer.status == ERR_GIVEUP_UPLOAD) {
			sb.append(resources.getString(R.string.uploadFailed));
			stateControl |= TransferInfo.FAILED;
		}
		else if (transfer.xfer_active) {
			// Currently transferring
			if (transfer.is_upload) {
				sb.append(resources.getString(R.string.uploading));
			}
			else {
				sb.append(resources.getString(R.string.downloading));
			}
			stateControl |= TransferInfo.RUNNING;
		}
		else {
			// Not transferring
			if (transfer.is_upload) {
				sb.append(resources.getString(R.string.uploadPending));
			}
			else {
				sb.append(resources.getString(R.string.downloadPending));
			}
		}
		if (transfer.project_backoff > 0) {
			sb.append(" (");
			sb.append(resources.getString(R.string.projectBackoff));
			sb.append(": ");
			sb.append(Formatter.formatElapsedTime(transfer.project_backoff));
			sb.append(")");
		}
		String state = sb.toString();
		return new TransferInfo(transfer.name,
				transfer.project_url,
				stateControl,
				progInd,
				projectName,
				progress,
				size,
				elapsed,
				speed,
				state);
	}
}
