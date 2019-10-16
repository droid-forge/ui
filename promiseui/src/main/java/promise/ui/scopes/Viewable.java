package promise.ui.scopes;

import androidx.annotation.LayoutRes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import promise.ui.model.ViewHolder;


@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Viewable {

  @LayoutRes int layoutResource() default 0;

  Class<? extends ViewHolder> viewHolderClass() default ViewHolder.class;
}
