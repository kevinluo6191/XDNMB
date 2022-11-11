//
//  PostsListRow.swift
//  iosApp
//
//  Created by 123哆3 on 2022/10/29.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct ThreadsListRow: View {
    var forumId: String
    var forumShowName: String
    var threadList: [shared.Thread]
    let sdk: XdSDK
    
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 16) {
                ForEach(threadList) {threads in
                    Threads(threads: threads, forumId: forumId, sdk: sdk)
                }
            }
            .padding()
            .frame(maxWidth: .infinity, maxHeight:.infinity)
            .background(Color (UIColor.systemGroupedBackground))
        }
        .background(Color (UIColor.systemGroupedBackground))
    }
}

func isAdmin(admin: Int32)->Color{
    if (admin == 0){
        return Color(UIColor.systemGray)
    } else {
        return Color(UIColor.systemRed)
    }
}

struct Threads: View {
    let threads: shared.Thread
    let forumId: String
    let sdk: XdSDK
    @Environment(\.colorScheme) var colorScheme
    @State private var htmlText: NSAttributedString?
    
    var body: some View{
        NavigationLink(destination: {
            InThreadView(viewModel: .init(sdk: sdk, threadId: Int(threads.id), page: 1), sdk: sdk, threadId: String(threads.id), forumId: forumId)
        }) {
            
            VStack(alignment: .leading) {
                HStack{
                    if(forumId == "-1"){
                        Text(sdk.getForumName(forumId: Int32(truncating: threads.fid!)))
                        Text("•")
                            .foregroundColor(Color.gray)
                    }
                    Text("No." + String(threads.id))
                        .foregroundColor(Color.gray)
                    if(threads.sage == 1){
                        Text("•")
                            .foregroundColor(Color.gray)
                        Text("SAGE")
                            .fontWeight(.semibold)
                            .foregroundColor(Color.red)
                    }
                    Spacer()
                    Label(sdk.formatTime(originalTime: threads.time, inThread: false), systemImage: "")
                        .labelStyle(.titleOnly)
                    Label("", systemImage: "clock")
                        .labelStyle(.iconOnly)
                }.padding(.bottom, 1.0).font(.caption).foregroundColor(Color(UIColor.label))
                
                VStack(alignment: .leading){
                    if(threads.title != "无标题"){
                        Text(threads.title)
                            .font(.headline)
                            .foregroundColor(Color(UIColor.label))
                            .multilineTextAlignment(.leading)
                    }
                    if(threads.name != "无名氏" && threads.name != "无标题" ){
                        Text(threads.name)
                            .font(.subheadline)
                            .foregroundColor(Color.gray)
                            .multilineTextAlignment(.leading)
                    }
                } .padding(.bottom, 1.0)
                
                VStack(alignment: .leading){
                    if let htmlText {
                        Text(AttributedString(htmlText))
                            .multilineTextAlignment(.leading)
                            .lineLimit(16)
                    }
                    
                    if (threads.img != "") {
                        AsyncImage(
                            url: URL(string:sdk.imgToUrl(img: threads.img, ext: threads.ext, isThumb: true))
                        ) { image in
                            image
                                .resizable()
                                .scaledToFit()
                        } placeholder: {
                            Color.gray.opacity(0.17)
                        }
                        .scaledToFit()
                        .frame(maxHeight: 100)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                }.onAppear{
                    DispatchQueue.main.async {
                        htmlText = threads.content.htmlAttributedString()
                    }
                }
                
                HStack{
                    Text(threads.userHash)
                        .foregroundColor(isAdmin(admin: threads.admin))
                    Spacer()
                    Label(String(Int(truncating: threads.replyCount ?? 0)) + "条回复", systemImage: "")
                        .labelStyle(.titleOnly)
                    Label("", systemImage: "bubble.right")
                        .labelStyle(.iconOnly)
                    
                }.padding(.top, 1.0).font(.caption).foregroundColor(Color(UIColor.label))
                
                
            }
            .padding(.vertical, 12)
            .padding(.horizontal, 18)
            .frame(maxWidth: .infinity)
            .background(Color (UIColor.secondarySystemGroupedBackground))
            .cornerRadius(16)
        }
    }
}
