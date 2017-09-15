package com.ogp.cputableau2;

import android.view.WindowManager;


interface ServiceInterface
{
	WindowManager			getWindowManager();
	PointF 					loadDefaultXY	();
	void 					saveDefaultXY	(float x, float y);
}
