import java.lang.reflect.Method;
import java.util.Random;

import sun.hotspot.WhiteBox;

public class WBTest {
	private static final WhiteBox WB = WhiteBox.getWhiteBox();
	private static final Random RANDOM = new Random();

	public static int foo() {
		return RANDOM.nextInt();
	}

	public static void main(String[] args) throws Exception {
		long sum = 0;
		int level = 0;
		boolean enqued = false;
		Method m = WBTest.class.getMethod("foo");
		
		for (int i = 0; i < 50_000; i++) {
			sum += foo();
			if (!enqued && WB.isMethodQueuedForCompilation(m)) {
				System.out.println(m + " enqued for compilation in iteration " + i);
				enqued = true;
			}
			if (WB.isMethodCompiled(m)) {
				if (WB.getMethodCompilationLevel(m) != level) {
					level = WB.getMethodCompilationLevel(m);
					System.out.println(m + " compiled at level " + level + " in iteration " + i);
					enqued = false;
				}
			}
		}
	}
}
