package burrows.apps.example.gif.presentation.main;

import android.util.Log;
import burrows.apps.example.gif.data.rest.model.RiffsyResponse;
import burrows.apps.example.gif.data.rest.repository.RiffsyRepository;
import burrows.apps.example.gif.presentation.IBaseSchedulerProvider;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * @author <a href="mailto:jaredsburrows@gmail.com">Jared Burrows</a>
 */
final class MainPresenter implements IMainPresenter {
  private final CompositeDisposable disposable = new CompositeDisposable();
  final IMainView view;
  private final RiffsyRepository repository;
  private final IBaseSchedulerProvider provider;

  public MainPresenter(IMainView view, RiffsyRepository repository, IBaseSchedulerProvider provider) {
    this.view = view;
    this.repository = repository;
    this.provider = provider;

    this.view.setPresenter(this);
  }

  @Override public void subscribe() {
  }

  @Override public void unsubscribe() {
    disposable.clear();
  }

  @Override public void clearImages() {
    // Clear current data
    view.clearImages();
  }

  /**
   * Load gif trending images.
   */
  @Override public void loadTrendingImages(Float next) {
    loadImages(repository.getTrendingResults(RiffsyRepository.DEFAULT_LIMIT_COUNT, next));
  }

  /**
   * Search gifs based on user input.
   *
   * @param searchString User input.
   */
  @Override public void loadSearchImages(String searchString, Float next) {
    loadImages(repository.getSearchResults(searchString, RiffsyRepository.DEFAULT_LIMIT_COUNT, next));
  }

  /**
   * Common code for subscription.
   *
   * @param observable Observable to added to the subscription.
   */
  public void loadImages(Observable<RiffsyResponse> observable) {
    disposable.add(observable
      .subscribeOn(provider.io())
      .observeOn(provider.ui())
      .subscribe(response -> {
        // onNext
        if (!view.isActive()) return;

        // Iterate over data from response and grab the urls
        view.addImages(response);
      }, error -> {
        // onError
        Log.e(TAG, "onError", error); // java.lang.UnsatisfiedLinkError - unit tests
      }, () -> {
        // onComplete
        Log.i(TAG, "Done loading!"); // java.lang.UnsatisfiedLinkError - unit tests
      }));
  }
}