package values.csi
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class Fragment2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Fragment에서 옵션 메뉴를 사용할 경우
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment22, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // onViewCreated 메서드에서 필요한 초기화 작업을 수행합니다.
        val contentListFragment = ContentListFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.com_container, contentListFragment)
            .commit()
    }
}
