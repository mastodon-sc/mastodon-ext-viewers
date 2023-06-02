/*-
 * #%L
 * mastodon-tomancak
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.tomancak;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;
import javax.swing.JFileChooser;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.IOException;
import mpicbg.spim.data.SpimDataException;

/**
 * Starts Mastodon on a given project file.
 */
public class StartMastodonOnProject {

	public static void main(String[] args) {
		try {
			String projectPath = "/home/ulman/Mette/CTC_screenRecordings_For_CzechTV_CBIA/PhC-C2DL-PSC/01_forFlatViewTesting.mastodon";

			//not sure what this is good for but see it everywhere...
			//(seems to give no effect on Linux)
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );

			//the central hub, a container to hold all
			final WindowManager windowManager = new WindowManager( new Context() );
			windowManager.getProjectManager().open( new MamutProjectIO().load( projectPath ) );

			//a GUI element wrapping around the hub
			final MainWindow win = new MainWindow(windowManager);

			//this makes the true Mastodon window visible
			//note: you can open project that restores/reopen e.g. TrackScheme window,
			//      yet the main Mastodon window is not shown... but this is runs non-stop
			win.setVisible( true );

			//this makes the whole thing (incl. the central hub) go down when the GUI is closed
			win.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		} catch (IOException | SpimDataException e) {
			e.printStackTrace();
		}
	}
}
