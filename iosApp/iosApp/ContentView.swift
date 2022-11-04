import SwiftUI
import shared

struct ContentView: View {
    let sdk = XdSDK(databaseDriverFactory: DatabaseDriverFactory())
    
	var body: some View {
        TabView{
            TimelineView(viewModel: .init(sdk: sdk), sdk: sdk)
                .tabItem{
                    Image(systemName: "clock.fill")
                    Text("时间线")
                }
            
            ForumView(viewModel: .init(sdk: sdk))
                .tabItem{
                    Image(systemName: "tray.2.fill")
                    Text("板块")
                }
            
            Text("TODO")
                .tabItem{
                    Image(systemName: "gear")
                    Text("选项")
                }
            
        }
	}
}

extension ForumGroup: Identifiable { }
extension Forum_: Identifiable { }
extension Forum: Identifiable { }
extension shared.Thread: Identifiable { }
