package com.meiji.toutiao.module.wenda.content;

import android.util.Log;

import com.meiji.toutiao.InitApp;
import com.meiji.toutiao.RetrofitFactory;
import com.meiji.toutiao.api.IMobileWendaApi;
import com.meiji.toutiao.bean.wenda.WendaContentBean;
import com.meiji.toutiao.module.wenda.detail.WendaDetailActivity;
import com.meiji.toutiao.utils.NetWorkUtil;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Meiji on 2017/5/22.
 */

class WendaContentPresenter implements IWendaContent.Presenter {

    private static final String TAG = "WendaContentPresenter";
    private IWendaContent.View view;
    private String qid;
    private int niceOffset = 0;
    private int normalOffset = 0;
    private int niceAnsCount = 0;
    private int normalAnsCount = 0;
    private List<WendaContentBean.AnsListBean> ansList = new ArrayList<>();
    private String title;

    WendaContentPresenter(IWendaContent.View view) {
        this.view = view;
    }

    public void doRefresh() {
        if (ansList.size() != 0) {
            ansList.clear();
            niceOffset = 0;
            normalOffset = 0;
        }
        doLoadData(this.qid);
    }

    @Override
    public void doShowNetError() {
        view.onHideLoading();
        view.onShowNetError();
    }

    @Override
    public void doLoadData(String qid) {
        this.qid = qid;
        Log.d(TAG, "doLoadData: " + qid);

        RetrofitFactory.getRetrofit().create(IMobileWendaApi.class).getWendaNiceContent(qid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WendaContentBean>() {
                    @Override
                    public void accept(@NonNull WendaContentBean wendaContentBean) throws Exception {
                        doSetHeader(wendaContentBean.getQuestion());
                        doSetAdapter(wendaContentBean.getAns_list());
                        niceOffset += 10;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        if (NetWorkUtil.isNetworkConnected(InitApp.AppContext)) {
                            view.onRefresh();
                        } else {
                            doShowNetError();
                        }
                    }
                });
    }

    @Override
    public void doLoadMoreData() {

        if (niceOffset < niceAnsCount) {
            RetrofitFactory.getRetrofit().create(IMobileWendaApi.class)
                    .getWendaNiceContentLoadMore(qid, niceOffset)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<WendaContentBean>() {
                        @Override
                        public void accept(@NonNull WendaContentBean wendaContentBean) throws Exception {
                            doSetAdapter(wendaContentBean.getAns_list());
                            niceOffset += 10;
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            if (NetWorkUtil.isNetworkConnected(InitApp.AppContext)) {
                                view.onRefresh();
                            } else {
                                doShowNetError();
                            }
                        }
                    });
        } else if (normalOffset < normalAnsCount) {
            RetrofitFactory.getRetrofit().create(IMobileWendaApi.class)
                    .getWendaNormalContentLoadMore(qid, normalOffset)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<WendaContentBean>() {
                        @Override
                        public void accept(@NonNull WendaContentBean wendaContentBean) throws Exception {
                            doSetAdapter(wendaContentBean.getAns_list());
                            normalOffset += 10;
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            if (NetWorkUtil.isNetworkConnected(InitApp.AppContext)) {
                                view.onRefresh();
                            } else {
                                doShowNetError();
                            }
                        }
                    });
        } else {
            doShowNoMore();
        }
    }

    @Override
    public void doSetAdapter(List<WendaContentBean.AnsListBean> list) {
        ansList.addAll(list);
        view.onSetAdapter(ansList);
        view.onHideLoading();
    }

    @Override
    public void doSetHeader(WendaContentBean.QuestionBean questionBean) {
        this.niceAnsCount = questionBean.getNice_ans_count();
        this.normalAnsCount = questionBean.getNormal_ans_count();
        this.title = questionBean.getTitle();
        view.onSetHeader(questionBean);
    }

    @Override
    public void doShowNoMore() {
        view.onHideLoading();
        view.onShowNoMore();
    }

    @Override
    public void doOnClickItem(int position) {
        WendaContentBean.AnsListBean ansListBean = ansList.get(position);
        ansListBean.setTitle(this.title);
        WendaDetailActivity.launch(ansListBean);
        Log.d(TAG, "doOnClickItem: " + ansList.get(position).getShare_data().getShare_url());
    }
}