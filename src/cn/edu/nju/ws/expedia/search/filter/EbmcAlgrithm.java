package cn.edu.nju.ws.expedia.search.filter;

public class EbmcAlgrithm {

	double buf[], p[][], sg_ps[];
	double w[], sg_ws[];
	int sg_f[];
	double l;
	int n, m;
	double sg_sp, sg_sw;	
	double sk_sp,sk_sw;
	int sk_f[];
	double sk_ws[],sk_ps[];
	boolean sk_ExistPosDensity()
	{
		for (int t = 1; t <= n; ++t) {
			if (sk_ws[t] + sk_sw > l) {
				continue;
			}
			sk_ps[t] = 0;
			for (int j = 1; j <= m; ++j) {
				double delta = p[t][j] - p[sk_f[j]][j];
				if (delta > 0) {
					sk_ps[t] += delta;
				}
			}
			if (sk_ws[t]==0 && sk_ps[t] > 0.0) {
				return true;
			}
			if (sk_ps[t]/sk_ws[t] > 0.0) {
				return true;
			}
		}
		return false;
	}
	int sk_GetMaxDensity()
	{
		double mxd = -1.0;
		int i = 0;
		for (int t = 1; t <= n; ++t) {
			if (sk_ws[t] + sk_sw > l) {
				continue;
			}
			sk_ps[t] = 0;
			for (int j = 1; j <= m; ++j) {
				double delta = p[t][j] - p[sk_f[j]][j];
				if (delta > 0) {
					sk_ps[t] += delta;
				}
			}
			if (sk_ws[t] == 0 && sk_ps[t] > 0) {
				return t;
			}
			if (sk_ps[t]/sk_ws[t] > mxd) {
				mxd = sk_ps[t] / sk_ws[t];
				i = t;
			}	
		}
		return i;
	}
	void sk_AddIn(int i)
	{
		sk_sw += sk_ws[i];
		sk_ws[i] = 0;
		for (int j = 1; j <= m; ++j) {
			double delta = p[i][j] - p[sk_f[j]][j];
			if (delta > 0) {
				sk_sp += delta;
				sk_f[j] = i;
			}
		}
	}
	int sk_Greedy()
	{
		while (sk_ExistPosDensity()) {
			int i = sk_GetMaxDensity();
			sk_AddIn(i);
		}
		return 0;
	}
	int sk_GetMaxProfit()
	{
		double mxp = -1.0;
		int i = 0;
		for (int t = 1; t <= n; ++t) {
			if (sk_ws[t] + sk_sw > l) {
				continue;
			}
			sk_ps[t] = 0;
			for (int j = 1; j <= m; ++j) {
				double delta = p[t][j] - p[sk_f[j]][j];
				if (delta > 0) {
					sk_ps[t] += delta;
				}
			}
			if (sk_ps[t] > mxp) {
				mxp = sk_ps[t] / sk_ws[t];
				i = t;
			}	
		}
		return i;
	}
	boolean sg_ExistPosDensity()
	{
		for (int t = 1; t <= n; ++t) {
			if (sg_ws[t] + sg_sw > l) {
				continue;
			}
			sg_ps[t] = 0;
			for (int j = 1; j <= m; ++j) {
				double delta = p[t][j] - p[sg_f[j]][j];
				if (delta > 0) {
					sg_ps[t] += delta;
				}
			}
			if (sg_ws[t]==0 && sg_ps[t] > 0.0) {
				return true;
			}
			if (sg_ps[t]/sg_ws[t] > 0.0) {
				return true;
			}
		}
		return false;
	}
	int sg_GetMaxDensity()
	{
		double mxd = -1.0;
		int i = 0;
		for (int t = 1; t <= n; ++t) {
			if (sg_ws[t] + sg_sw > l) {
				continue;
			}
			sg_ps[t] = 0;
			for (int j = 1; j <= m; ++j) {
				double delta = p[t][j] - p[sg_f[j]][j];
				if (delta > 0) {
					sg_ps[t] += delta;
				}
			}
			if (sg_ws[t] == 0 && sg_ps[t] > 0) {
				return t;
			}
			if (sg_ps[t]/sg_ws[t] > mxd) {
				mxd = sg_ps[t] / sg_ws[t];
				i = t;
			}	
		}
		return i;
	}
	void sg_AddIn(int i)
	{
		sg_sw += sg_ws[i];
		sg_ws[i] = 0;
		for (int j = 1; j <= m; ++j) {
			double delta = p[i][j] - p[sg_f[j]][j];
			if (delta > 0) {
				sg_sp += delta;
				sg_f[j] = i;
			}
		}
	}
	int sg_Greedy()
	{
		while (sg_ExistPosDensity()) {
			int i = sg_GetMaxDensity();
			sg_AddIn(i);
		}
		return 0;
	}
	
	
	void SetN(int num)
	{
		this.n=num;
		w = new double[n+1];
	}
	void SetM(int num)
	{
		this.m=num;
	}
	void SetL(double budget)
	{
		this.l=budget;
	}
	void SetWeight(double []weight)
	{
		for(int i=1 ; i<=n; i++)
		{
			this.w[i]=weight[i-1];
		}
	}
	void SetProfit(int a[],int b[],double c[])
	{
		p = new double[n+1][m+1];
		for(int i=0;i<a.length;i++)
		{
			p[a[i]+1][b[i]+1]=c[i];
		}
	}
	
	void Initailize()
	{
		sg_f = new int[m+1];
		sg_ps = new double[n+1];
		sg_ws = new double[n+1];

		sk_f = new int[m+1];
		sk_ps = new double[n+1];
		sk_ws = new double[n+1];
		for(int i=0;i<n+1;i++)
		{
			sk_ws[i] = w[i];
			sg_ws[i] = w[i];
		}
	}
	
	void  sg_run()
	{
		sg_sp = sg_sw = 0;
		sg_Greedy();
	}
	void sk_run()
	{
		sk_sp = sk_sw = 0;
		int k = sk_GetMaxProfit();
		sk_AddIn(k);
		sk_Greedy();
	}
	public int[] getSelectID()
	{
		Initailize();
		sg_run();
		sk_run();
		boolean isout[]=new boolean[n+1];
		int ans[];
		int cnt=0;
		if(sk_sp>sg_sp)
		{
			for(int i=1;i<=m;i++)
			{
				if(isout[sk_f[i]]==false&&sk_f[i]!=0)
				{
					cnt++;
					isout[sk_f[i]]=true;
				}
			}
			ans=new int[cnt];
			cnt=0;
			for(int i=1;i<=n;i++)
			{
				if(isout[i]){
					ans[cnt]=i-1;
					cnt++;
				}
			}
		}
		else 
		{
			for(int i=1;i<=m;i++)
			{
				if(isout[sg_f[i]]==false&&sg_f[i]!=0)
				{
					cnt++;
					isout[sg_f[i]]=true;
				}
			}
			ans=new int[cnt];
			cnt=0;
			for(int i=1;i<=n;i++)
			{
				if(isout[i]){
					ans[cnt]=i-1;
					cnt++;
				}
			}
		}
		return ans;
	}
}
